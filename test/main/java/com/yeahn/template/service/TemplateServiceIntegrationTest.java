package main.java.com.yeahn.template.service;

import com.yeahn.Application;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import com.yeahn.template.service.TemplateService;
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
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test_admin", "password")
        );
        request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
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

        // [Then] 2. 연동된 운동 정보(Exercise) 상세 검증 (DB 직접 조회로 데이터 정합성 확인)
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

    private TemplateDto createExerciseDto(String name) {
        TemplateDto dto = new TemplateDto();
        dto.setTplExerName(name);
        dto.setTplTypeCode("MULTI_TYPE");
        return dto;
    }
}
