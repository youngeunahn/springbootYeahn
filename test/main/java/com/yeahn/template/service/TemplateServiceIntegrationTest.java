package com.yeahn.template.service;

import com.yeahn.Application;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TemplateService 통합 테스트
 * 실무 수준의 엄격한 데이터 정합성 검증을 수행합니다.
 */
@SpringBootTest(classes = Application.class)
@Transactional
public class TemplateServiceIntegrationTest {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private JdbcTemplate jdbcTemplate; // DB 직접 검증을 위해 사용

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        // 기존 시큐리티 컨텍스트 초기화
        SecurityContextHolder.clearContext();
        
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test_admin", "password")
        );
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("템플릿 및 연동된 운동 정보의 생성/조회 전체 흐름 검증")
    void templateAndExerciseFullLifecycle_Test() {
        // [Given] 고유한 테스트 데이터 준비
        String uniqueTplName = "TPL_" + UUID.randomUUID().toString().substring(0, 8);
        String exerciseName = "EX_" + UUID.randomUUID().toString().substring(0, 5);
        String testTypeCode = "TYPE_A";

        TemplateDto createRequest = new TemplateDto();
        createRequest.setTplName(uniqueTplName);
        createRequest.setTplPhase("1단계");
        createRequest.setTplTypeCode(testTypeCode);
        createRequest.setTplSortOrder(1);

        TemplateDto exercise = new TemplateDto();
        exercise.setTplTypeCode(testTypeCode);
        exercise.setTplExerName(exerciseName);
        createRequest.setExercises(Arrays.asList(exercise));

        // [When] 생성 실행
        Long generatedTplSeq = templateService.createTemplate(createRequest, request);

        // [Then] 1. 템플릿 목록 조회 검증 (GROUP BY가 적용된 목록 조회)
        TemplateSearchDto searchDto = new TemplateSearchDto();
        searchDto.setTplName(uniqueTplName);
        List<TemplateDto> searchResult = templateService.searchTplList(searchDto);

        assertFalse(searchResult.isEmpty(), "생성한 템플릿 이름으로 검색 결과가 존재해야 합니다.");
        assertEquals(1, searchResult.size(), "중복 없이 1개의 템플릿만 조회되어야 합니다.");

        // [Then] 2. 템플릿 상세 조회 검증 (Service 메서드 활용)
        TemplateDto detailResult = templateService.getTplDetail(generatedTplSeq);
        assertNotNull(detailResult, "상세 조회 결과가 존재해야 합니다.");
        assertEquals(uniqueTplName, detailResult.getTplName());
        assertNotNull(detailResult.getExercises(), "연동된 운동 리스트가 포함되어야 합니다.");
        assertEquals(1, detailResult.getExercises().size());
        assertEquals(exerciseName, detailResult.getExercises().get(0).getTplExerName());

        // [Then] 3. 연동된 운동 정보(Exercise) 상세 검증 (DB 직접 조회로 데이터 정합성 확인)
        Integer exerciseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_EXER WHERE TPL_SEQ = ?", Integer.class, generatedTplSeq);
        assertEquals(1, exerciseCount, "연동된 운동 데이터가 DB에 1개 존재해야 합니다.");

        String savedExerName = jdbcTemplate.queryForObject(
                "SELECT attr.TPL_EXER_NAME FROM TB_EXER_ATTR attr " +
                "INNER JOIN TB_EXER exer ON attr.TPL_ATTR_SEQ = exer.TPL_ATTR_SEQ " +
                "WHERE exer.TPL_SEQ = ?", String.class, generatedTplSeq);
        assertEquals(exerciseName, savedExerName, "저장된 운동 이름이 요청값과 일치해야 합니다.");
    }

    @Test
    @DisplayName("복합 운동 정보 포함 시 저장된 개수 및 DB 정합성 검증")
    void multipleExercises_CountAndIntegrity_Test() {
        // [Given] 3개의 운동을 포함한 템플릿 생성 요청
        String complexTplName = "COMPLEX_" + UUID.randomUUID().toString().substring(0, 8);
        TemplateDto createRequest = new TemplateDto();
        createRequest.setTplName(complexTplName);
        createRequest.setTplTypeCode("MULTI_TYPE");
        
        createRequest.setExercises(Arrays.asList(
                createExerciseDto("운동1"), createExerciseDto("운동2"), createExerciseDto("운동3")
        ));

        // [When] 생성 실행
        Long tplSeq = templateService.createTemplate(createRequest, request);

        // [Then] 1. 템플릿 목록 조회 (정상 1건 확인)
        TemplateDto listParam = new TemplateDto();
        listParam.setTplTypeCode("MULTI_TYPE");
        List<TemplateDto> results = templateService.getTplList(listParam);
        assertTrue(results.stream().anyMatch(t -> t.getTplSeq().equals(tplSeq)));

        // [Then] 2. 실제 저장된 운동 개수 검증 (DB 직접 조회)
        Integer savedExerciseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_EXER WHERE TPL_SEQ = ?", Integer.class, tplSeq);

        assertEquals(3, savedExerciseCount, "DB에 저장된 운동 매핑 데이터 개수가 3개여야 합니다.");
    }

    @Test
    @DisplayName("운동 순서 변경 통합 테스트 - DB 반영 여부 확인")
    void reorderExercises_Integration_Test() {
        // [Given] 3개의 운동이 포함된 템플릿 생성
        TemplateDto createRequest = new TemplateDto();
        createRequest.setTplName("ORDER_TEST");
        createRequest.setTplTypeCode("ORDER_TYPE");
        createRequest.setExercises(Arrays.asList(
                createExerciseDto("운동1"), createExerciseDto("운동2"), createExerciseDto("운동3")
        ));

        Long tplSeq = templateService.createTemplate(createRequest, request);

        // 생성된 운동들의 tplAttrSeq 목록 조회
        List<Long> attrSeqs = jdbcTemplate.queryForList(
                "SELECT TPL_ATTR_SEQ FROM TB_EXER WHERE TPL_SEQ = ? ORDER BY TPL_ATTR_SEQ",
                Long.class, tplSeq
        );

        // [When] 순서 변경 요청 구성 (역순으로 변경: 3, 2, 1)
        TemplateDto reorderRequest = new TemplateDto();
        reorderRequest.setTplSeq(tplSeq);

        TemplateDto ex1 = new TemplateDto();
        ex1.setTplAttrSeq(attrSeqs.get(0));
        ex1.setTplSortOrder(3); // 원래 1 -> 3

        TemplateDto ex2 = new TemplateDto();
        ex2.setTplAttrSeq(attrSeqs.get(1));
        ex2.setTplSortOrder(2); // 원래 2 -> 2

        TemplateDto ex3 = new TemplateDto();
        ex3.setTplAttrSeq(attrSeqs.get(2));
        ex3.setTplSortOrder(1); // 원래 3 -> 1

        reorderRequest.setExercises(Arrays.asList(ex1, ex2, ex3));

        // 순서 변경 실행
        templateService.reorderExercises(reorderRequest, request);

        // [Then] DB에서 변경된 순서 검증
        Integer order1 = jdbcTemplate.queryForObject(
                "SELECT TPL_SORT_ORDER FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?",
                Integer.class, attrSeqs.get(0));
        Integer order2 = jdbcTemplate.queryForObject(
                "SELECT TPL_SORT_ORDER FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?",
                Integer.class, attrSeqs.get(1));
        Integer order3 = jdbcTemplate.queryForObject(
                "SELECT TPL_SORT_ORDER FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?",
                Integer.class, attrSeqs.get(2));

        assertEquals(3, order1, "운동1의 순서가 3으로 변경되어야 합니다.");
        assertEquals(2, order2, "운동2의 순서가 2로 유지되어야 합니다.");
        assertEquals(1, order3, "운동3의 순서가 1로 변경되어야 합니다.");

        // UPD_USER_ID 검증
        String updUser = jdbcTemplate.queryForObject(
                "SELECT UPD_USER_ID FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?",
                String.class, attrSeqs.get(0));
        assertEquals("test_admin", updUser, "수정자 ID가 올바르게 저장되어야 합니다.");
    }

    @Test
    @DisplayName("템플릿 수정 통합 테스트 - 업데이트/추가/삭제 복합 검증")
    void templateUpdate_Integration_Test() {
        // [Given] 1. 초기 데이터 생성 (운동 2개 포함)
        TemplateDto createRequest = new TemplateDto();
        createRequest.setTplName("INIT_TPL");
        createRequest.setTplTypeCode("UPDATE_TEST");

        TemplateDto ex1 = createExerciseDto("기존운동1");
        TemplateDto ex2 = createExerciseDto("기존운동2");
        createRequest.setExercises(Arrays.asList(ex1, ex2));

        Long tplSeq = templateService.createTemplate(createRequest, request);

        // 생성된 운동 ID(tplAttrSeq) 가져오기
        List<Long> attrSeqs = jdbcTemplate.queryForList(
                "SELECT TPL_ATTR_SEQ FROM TB_EXER WHERE TPL_SEQ = ? ORDER BY TPL_ATTR_SEQ", Long.class, tplSeq);
        Long idToKeep = attrSeqs.get(0);
        Long idToDelete = attrSeqs.get(1);

        // [When] 2. 수정 요청 구성
        TemplateDto updateRequest = new TemplateDto();
        updateRequest.setTplSeq(tplSeq);
        updateRequest.setTplName("UPDATED_TPL"); // 이름 변경
        updateRequest.setTplTypeCode("UPDATE_TEST");

        // (1) 기존 운동1 유지 및 이름 변경
        TemplateDto exKeep = new TemplateDto();
        exKeep.setTplAttrSeq(idToKeep);
        exKeep.setTplExerName("이름변경운동1");
        exKeep.setTplSortOrder(1);

        // (2) 신규 운동 추가 (기존 운동2는 리스트에서 제외하여 삭제 유도)
        TemplateDto exNew = createExerciseDto("신규추가운동");
        exNew.setTplSortOrder(2);

        updateRequest.setExercises(Arrays.asList(exKeep, exNew));

        templateService.updateTemplate(updateRequest, request);

        // [Then] 3. DB 정합성 검증
        // 템플릿 마스터 정보 변경 확인
        String updatedTplName = jdbcTemplate.queryForObject(
                "SELECT TPL_NAME FROM TB_EXER_TPL WHERE TPL_SEQ = ?", String.class, tplSeq);
        assertEquals("UPDATED_TPL", updatedTplName);

        // 기존 운동1: 이름 변경 및 유지 확인
        String nameAfterUpdate = jdbcTemplate.queryForObject(
                "SELECT TPL_EXER_NAME FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?", String.class, idToKeep);
        assertEquals("이름변경운동1", nameAfterUpdate);

        // 기존 운동2: Soft Delete(DEL_YN='Y') 확인
        String delYn = jdbcTemplate.queryForObject(
                "SELECT DEL_YN FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?", String.class, idToDelete);
        assertEquals("Y", delYn);

        // 신규 운동: 추가 확인
        Integer newExCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_EXER_ATTR attr " +
                        "INNER JOIN TB_EXER exer ON attr.TPL_ATTR_SEQ = exer.TPL_ATTR_SEQ " +
                        "WHERE exer.TPL_SEQ = ? AND attr.TPL_EXER_NAME = '신규추가운동'", Integer.class, tplSeq);
        assertEquals(1, newExCount);
    }

    @Test
    @DisplayName("템플릿 삭제 통합 테스트 - Soft Delete 검증")
    void templateDelete_Integration_Test() {
        // [Given] 삭제할 템플릿 생성
        TemplateDto createRequest = new TemplateDto();
        createRequest.setTplName("DELETE_ME");
        createRequest.setTplTypeCode("DEL_TEST");
        createRequest.setExercises(Arrays.asList(createExerciseDto("삭제될운동")));

        Long tplSeq = templateService.createTemplate(createRequest, request);
        Long attrSeq = jdbcTemplate.queryForObject(
                "SELECT TPL_ATTR_SEQ FROM TB_EXER WHERE TPL_SEQ = ?", Long.class, tplSeq);

        // [When] 삭제 실행
        templateService.deleteTemplate(tplSeq, request);

        // [Then] 1. 템플릿 마스터 Soft Delete 확인
        String tplDelYn = jdbcTemplate.queryForObject(
                "SELECT DEL_YN FROM TB_EXER_TPL WHERE TPL_SEQ = ?", String.class, tplSeq);
        assertEquals("Y", tplDelYn);

        // [Then] 2. 운동 속성 Soft Delete 확인
        String attrDelYn = jdbcTemplate.queryForObject(
                "SELECT DEL_YN FROM TB_EXER_ATTR WHERE TPL_ATTR_SEQ = ?", String.class, attrSeq);
        assertEquals("Y", attrDelYn);

        // [Then] 3. 관계 데이터 삭제 확인
        Integer relCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM TB_EXER WHERE TPL_SEQ = ?", Integer.class, tplSeq);
        assertEquals(0, relCount, "관계 정보는 물리 삭제되어야 합니다.");
    }

    private TemplateDto createExerciseDto(String name) {
        TemplateDto dto = new TemplateDto();
        dto.setTplExerName(name);
        dto.setTplTypeCode("MULTI_TYPE");
        dto.setTplCategoryCode("CAT01");
        dto.setTplKindCode("KIND01");
        return dto;
    }
}
