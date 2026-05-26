package com.yeahn.template.service;

import com.yeahn.template.dao.TemplateMapper;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TemplateService 단위 테스트
 *
 * - DB 없이 Mockito로 TemplateMapper를 대체하여 서비스 로직만 검증합니다.
 * - SecurityContextHolder에 테스트 인증 정보를 넣어 로그인 사용자 ID를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class TemplateServiceUnitTest {

    @Mock
    private TemplateMapper templateMapper;

    @InjectMocks
    private TemplateService templateService;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        setAuthenticatedUser("testUser");
        lenient().when(request.getRemoteAddr()).thenReturn("10.10.10.10");
    }

    @AfterEach
    void tearDown() {
        // 테스트 격리: SecurityContext 상태가 다음 테스트에 영향을 주지 않도록 초기화합니다.
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, "password")
        );
    }

    private void setRequestContext(String userId, String ip) {
        setAuthenticatedUser(userId);
        when(request.getRemoteAddr()).thenReturn(ip);
    }

    // ────────────────────────────────────────────────────────────────
    // 조회 테스트
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("템플릿 목록을 조회한다")
    void getTplList_Test() {
        // [Given]
        TemplateDto dto = new TemplateDto();
        List<TemplateDto> expectedList = Arrays.asList(new TemplateDto(), new TemplateDto());
        when(templateMapper.getTplList(dto)).thenReturn(expectedList);

        // [When]
        List<TemplateDto> result = templateService.getTplList(dto);

        // [Then]
        assertEquals(expectedList.size(), result.size());
        verify(templateMapper, times(1)).getTplList(dto);
    }

    @Test
    @DisplayName("템플릿을 검색한다")
    void searchTplList_Test() {
        // [Given]
        TemplateSearchDto dto = new TemplateSearchDto();
        List<TemplateDto> expectedList = Arrays.asList(new TemplateDto());
        when(templateMapper.searchTplList(dto)).thenReturn(expectedList);

        // [When]
        List<TemplateDto> result = templateService.searchTplList(dto);

        // [Then]
        assertEquals(expectedList.size(), result.size());
        verify(templateMapper, times(1)).searchTplList(dto);
    }

    @Test
    @DisplayName("템플릿 상세와 운동 목록을 조회한다")
    void getTplDetail_Success_Test() {
        // [Given]
        Long tplSeq = 1L;
        TemplateDto mockTpl = new TemplateDto();
        mockTpl.setTplSeq(tplSeq);
        mockTpl.setTplName("상세 테스트");

        List<TemplateDto> mockExerList = Arrays.asList(new TemplateDto(), new TemplateDto());

        when(templateMapper.getTplDetail(tplSeq)).thenReturn(mockTpl);
        when(templateMapper.getExerList(tplSeq)).thenReturn(mockExerList);

        // [When]
        TemplateDto result = templateService.getTplDetail(tplSeq);

        // [Then]
        assertNotNull(result);
        assertEquals(tplSeq, result.getTplSeq());
        assertEquals(2, result.getExercises().size());
        verify(templateMapper, times(1)).getTplDetail(tplSeq);
        verify(templateMapper, times(1)).getExerList(tplSeq);
    }

    @Test
    @DisplayName("템플릿이 없으면 상세 조회에서 null을 반환한다")
    void getTplDetail_NotFound_Test() {
        // [Given]
        Long tplSeq = 999L;
        when(templateMapper.getTplDetail(tplSeq)).thenReturn(null);

        // [When]
        TemplateDto result = templateService.getTplDetail(tplSeq);

        // [Then]
        assertNull(result);
        verify(templateMapper, times(1)).getTplDetail(tplSeq);
        // getTplDetail이 null이면 getExerList는 호출되면 안 됩니다.
        verify(templateMapper, never()).getExerList(anyLong());
    }

    // ────────────────────────────────────────────────────────────────
    // 생성/수정/삭제 테스트
    // ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("운동이 포함된 템플릿을 생성한다")
    void createTemplate_WithExercises_Test() {
        // [Given]
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplName("테스트 템플릿");
        requestDto.setTplPhase("1단계");
        requestDto.setTplSortOrder(1);

        TemplateDto exercise = new TemplateDto();
        exercise.setTplAttrSeq(100L);
        requestDto.setExercises(Arrays.asList(exercise));

        // insertTemplate 호출 시 tplSeq를 10L로 세팅 (DB AUTO_INCREMENT 시뮬레이션)
        doAnswer(invocation -> {
            TemplateDto dto = invocation.getArgument(0);
            dto.setTplSeq(10L);
            return null;
        }).when(templateMapper).insertTemplate(any(TemplateDto.class));

        // [When]
        Long tplSeq = templateService.createTemplate(requestDto, request);

        // [Then] 반환 PK 확인
        assertEquals(10L, tplSeq);

        // ArgumentCaptor로 insertTemplate에 실제 전달된 TemplateDto 캡처
        // any()만으로는 insUserId, delYn 등 내부 필드가 올바르게 세팅되었는지 알 수 없습니다.
        ArgumentCaptor<TemplateDto> insertCaptor = ArgumentCaptor.forClass(TemplateDto.class);
        verify(templateMapper).insertTemplate(insertCaptor.capture());
        TemplateDto capturedInsert = insertCaptor.getValue();

        // 감사 필드: 서비스가 SecurityContext에서 읽어 자동으로 채워야 합니다.
        assertEquals("testUser", capturedInsert.getInsUserId(), "insUserId가 로그인 사용자여야 합니다.");
        assertEquals("N", capturedInsert.getDelYn(), "신규 생성 시 delYn은 'N'이어야 합니다.");

        verify(templateMapper, times(1)).insertExercise(any(TemplateDto.class));
        verify(templateMapper, times(1)).insertRelation(eq(10L), any());
    }

    @Test
    @DisplayName("운동 없이 템플릿만 생성한다")
    void createTemplate_NoExercises_Test() {
        // [Given]
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplName("테스트 템플릿");
        requestDto.setExercises(null);

        doAnswer(invocation -> {
            TemplateDto dto = invocation.getArgument(0);
            dto.setTplSeq(20L);
            return null;
        }).when(templateMapper).insertTemplate(any(TemplateDto.class));

        // [When]
        Long tplSeq = templateService.createTemplate(requestDto, request);

        // [Then]
        assertEquals(20L, tplSeq);
        verify(templateMapper, times(1)).insertTemplate(any(TemplateDto.class));
        // 운동이 없으면 insertExercise와 insertRelation은 호출되면 안 됩니다.
        verify(templateMapper, never()).insertExercise(any(TemplateDto.class));
        verify(templateMapper, never()).insertRelation(anyLong(), any());
    }

    @Test
    @DisplayName("운동 순서를 변경한다")
    void reorderExercises_Test() {
        // [Given]
        setRequestContext("adminUser", "10.0.0.1");

        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplSeq(123L);

        TemplateDto ex1 = new TemplateDto();
        ex1.setTplAttrSeq(501L);
        ex1.setTplSortOrder(1);

        TemplateDto ex2 = new TemplateDto();
        ex2.setTplAttrSeq(502L);
        ex2.setTplSortOrder(2);

        requestDto.setExercises(Arrays.asList(ex1, ex2));

        // [When]
        templateService.reorderExercises(requestDto, request);

        // [Then]
        verify(templateMapper, times(1)).updateExerciseOrders(anyList());
        // 순서 변경 시에도 수정자 감사 필드가 채워져야 합니다.
        assertEquals("adminUser", ex1.getUpdUserId());
        assertEquals("10.0.0.1", ex1.getUpdIp());
        assertEquals("adminUser", ex2.getUpdUserId());
        assertEquals("10.0.0.1", ex2.getUpdIp());
    }

    @Test
    @DisplayName("템플릿 수정 시 운동을 수정하고 추가하고 삭제한다")
    void updateTemplate_Complex_Test() {
        // [Given]
        Long tplSeq = 100L;
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplSeq(tplSeq);
        requestDto.setTplName("수정된 템플릿");

        // 요청: 기존 501L 유지, 신규 항목 추가 (502L은 리스트에서 빠져 삭제 대상)
        TemplateDto exExisting = new TemplateDto();
        exExisting.setTplAttrSeq(501L);
        exExisting.setTplExerName("기존 운동 수정");

        TemplateDto exNew = new TemplateDto();
        exNew.setTplExerName("새로운 운동");

        requestDto.setExercises(Arrays.asList(exExisting, exNew));

        // DB에 현재 저장된 상태: 501L(유지), 502L(삭제 대상)
        TemplateDto currentEx1 = new TemplateDto(); currentEx1.setTplAttrSeq(501L);
        TemplateDto currentEx2 = new TemplateDto(); currentEx2.setTplAttrSeq(502L);
        when(templateMapper.getExerList(tplSeq)).thenReturn(Arrays.asList(currentEx1, currentEx2));

        // [When]
        templateService.updateTemplate(requestDto, request);

        // [Then]
        // 1. 마스터 업데이트 호출 확인
        verify(templateMapper, times(1)).updateTemplate(requestDto);

        // 2. 기존 항목(501L): 이름 수정 → updateExercise 호출
        verify(templateMapper, times(1)).updateExercise(argThat(ex -> ex.getTplAttrSeq().equals(501L)));

        // 3. 신규 항목(tplAttrSeq=null): insert 후 relation 연결
        verify(templateMapper, times(1)).insertExercise(argThat(ex -> ex.getTplAttrSeq() == null));
        verify(templateMapper, times(1)).insertRelation(eq(tplSeq), any());

        // 4. 요청 리스트에 없는 502L: 소프트 삭제 호출
        verify(templateMapper, times(1)).deleteExerciseBySeq(502L);
    }

    @Test
    @DisplayName("템플릿과 연결된 운동 정보를 삭제한다")
    void deleteTemplate_Test() {
        // [Given]
        setRequestContext("deleteUser", "2.2.2.2");

        Long tplSeq = 200L;

        // [When]
        templateService.deleteTemplate(tplSeq, request);

        // [Then] ArgumentCaptor로 deleteTemplate에 전달된 TemplateDto 캡처
        ArgumentCaptor<TemplateDto> deleteCaptor = ArgumentCaptor.forClass(TemplateDto.class);
        verify(templateMapper).deleteTemplate(deleteCaptor.capture());
        TemplateDto capturedDelete = deleteCaptor.getValue();

        // 삭제 시 수정자 감사 필드가 채워져야 합니다.
        assertEquals("deleteUser", capturedDelete.getUpdUserId(), "삭제 시 updUserId가 로그인 사용자여야 합니다.");
        assertEquals("2.2.2.2", capturedDelete.getUpdIp(), "삭제 시 updIp가 요청 IP여야 합니다.");

        // 템플릿 삭제 시 relation과 운동 속성도 함께 삭제됩니다.
        verify(templateMapper, times(1)).deleteRelationByTplSeq(tplSeq);
        verify(templateMapper, times(1)).deleteExerciseByTplSeq(any(TemplateDto.class));
    }
}
