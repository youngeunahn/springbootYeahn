package main.java.com.yeahn.template.service;

import com.yeahn.template.dao.TemplateMapper;
import com.yeahn.template.dto.TemplateDto;
import com.yeahn.template.dto.TemplateSearchDto;
import com.yeahn.template.service.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceUnitTest {

    @Mock
    private TemplateMapper templateMapper;

    @InjectMocks
    private TemplateService templateService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("템플릿 목록 조회 테스트")
    void getTplList_Test() {
        // given
        TemplateDto dto = new TemplateDto();
        List<TemplateDto> expectedList = Arrays.asList(new TemplateDto(), new TemplateDto());
        when(templateMapper.getTplList(dto)).thenReturn(expectedList);

        // when
        List<TemplateDto> result = templateService.getTplList(dto);

        // then
        assertEquals(expectedList.size(), result.size());
        verify(templateMapper, times(1)).getTplList(dto);
    }

    @Test
    @DisplayName("템플릿 검색 테스트")
    void searchTplList_Test() {
        // given
        TemplateSearchDto dto = new TemplateSearchDto();
        List<TemplateDto> expectedList = Arrays.asList(new TemplateDto());
        when(templateMapper.searchTplList(dto)).thenReturn(expectedList);

        // when
        List<TemplateDto> result = templateService.searchTplList(dto);

        // then
        assertEquals(expectedList.size(), result.size());
        verify(templateMapper, times(1)).searchTplList(dto);
    }

    @Test
    @DisplayName("템플릿 상세 조회 테스트 - 성공")
    void getTplDetail_Success_Test() {
        // given
        Long tplSeq = 1L;
        TemplateDto mockTpl = new TemplateDto();
        mockTpl.setTplSeq(tplSeq);
        mockTpl.setTplName("상세 테스트");

        List<TemplateDto> mockExerList = Arrays.asList(new TemplateDto(), new TemplateDto());
        
        when(templateMapper.getTplDetail(tplSeq)).thenReturn(mockTpl);
        when(templateMapper.getExerList(tplSeq)).thenReturn(mockExerList);

        // when
        TemplateDto result = templateService.getTplDetail(tplSeq);

        // then
        assertNotNull(result);
        assertEquals(tplSeq, result.getTplSeq());
        assertEquals(2, result.getExercises().size());
        verify(templateMapper, times(1)).getTplDetail(tplSeq);
        verify(templateMapper, times(1)).getExerList(tplSeq);
    }

    @Test
    @DisplayName("템플릿 상세 조회 테스트 - 데이터 없음")
    void getTplDetail_NotFound_Test() {
        // given
        Long tplSeq = 999L;
        when(templateMapper.getTplDetail(tplSeq)).thenReturn(null);

        // when
        TemplateDto result = templateService.getTplDetail(tplSeq);

        // then
        assertNull(result);
        verify(templateMapper, times(1)).getTplDetail(tplSeq);
        verify(templateMapper, never()).getExerList(anyLong());
    }

    @Test
    @DisplayName("템플릿 생성 테스트 - 운동 포함")
    void createTemplate_WithExercises_Test() {
        // given
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplName("테스트 템플릿");
        requestDto.setTplPhase("1단계");
        requestDto.setTplSortOrder(1);

        TemplateDto exercise = new TemplateDto();
        exercise.setTplAttrSeq(100L);
        requestDto.setExercises(Arrays.asList(exercise));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        // getIP(req) 내부에서 null 체크를 통과하도록 설정
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        doAnswer(invocation -> {
            TemplateDto dto = invocation.getArgument(0);
            dto.setTplSeq(10L);
            return null;
        }).when(templateMapper).insertTemplate(any(TemplateDto.class));

        // when
        Long tplSeq = templateService.createTemplate(requestDto, request);

        // then
        assertEquals(10L, tplSeq);
        verify(templateMapper, times(1)).insertTemplate(any(TemplateDto.class));
        verify(templateMapper, times(1)).insertExercise(any(TemplateDto.class));
        verify(templateMapper, times(1)).insertRelation(eq(10L), any());
    }

    @Test
    @DisplayName("템플릿 생성 테스트 - 운동 미포함")
    void createTemplate_NoExercises_Test() {
        // given
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplName("테스트 템플릿");
        requestDto.setExercises(null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");

        doAnswer(invocation -> {
            TemplateDto dto = invocation.getArgument(0);
            dto.setTplSeq(20L);
            return null;
        }).when(templateMapper).insertTemplate(any(TemplateDto.class));

        // when
        Long tplSeq = templateService.createTemplate(requestDto, request);

        // then
        assertEquals(20L, tplSeq);
        verify(templateMapper, times(1)).insertTemplate(any(TemplateDto.class));
        verify(templateMapper, never()).insertExercise(any(TemplateDto.class));
        verify(templateMapper, never()).insertRelation(anyLong(), any());
    }

    @Test
    @DisplayName("운동 순서 변경 테스트")
    void reorderExercises_Test() {
        // given
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplSeq(123L);

        TemplateDto ex1 = new TemplateDto();
        ex1.setTplAttrSeq(501L);
        ex1.setTplSortOrder(1);

        TemplateDto ex2 = new TemplateDto();
        ex2.setTplAttrSeq(502L);
        ex2.setTplSortOrder(2);

        requestDto.setExercises(Arrays.asList(ex1, ex2));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("adminUser");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        // when
        templateService.reorderExercises(requestDto, request);

        // then
        verify(templateMapper, times(1)).updateExerciseOrders(anyList());
        assertEquals("adminUser", ex1.getUpdUserId());
        assertEquals("10.0.0.1", ex1.getUpdIp());
        assertEquals("adminUser", ex2.getUpdUserId());
        assertEquals("10.0.0.1", ex2.getUpdIp());
    }

    @Test
    @DisplayName("템플릿 수정 테스트 - 업데이트/추가/삭제 복합 시나리오")
    void updateTemplate_Complex_Test() {
        // given
        Long tplSeq = 100L;
        TemplateDto requestDto = new TemplateDto();
        requestDto.setTplSeq(tplSeq);
        requestDto.setTplName("수정된 템플릿");

        // 1. 기존 항목 (유지 및 업데이트 대상)
        TemplateDto exExisting = new TemplateDto();
        exExisting.setTplAttrSeq(501L);
        exExisting.setTplExerName("기존 운동 수정");

        // 2. 신규 항목 (추가 대상)
        TemplateDto exNew = new TemplateDto();
        exNew.setTplExerName("새로운 운동");

        requestDto.setExercises(Arrays.asList(exExisting, exNew));

        // DB에 현재 저장된 상태 (501L: 유지, 502L: 삭제 대상)
        TemplateDto currentEx1 = new TemplateDto(); currentEx1.setTplAttrSeq(501L);
        TemplateDto currentEx2 = new TemplateDto(); currentEx2.setTplAttrSeq(502L);
        when(templateMapper.getExerList(tplSeq)).thenReturn(Arrays.asList(currentEx1, currentEx2));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("updateUser");
        when(request.getRemoteAddr()).thenReturn("1.1.1.1");

        // when
        templateService.updateTemplate(requestDto, request);

        // then
        // 1. 마스터 업데이트 검증
        verify(templateMapper, times(1)).updateTemplate(requestDto);

        // 2. 기존 항목(501L) 업데이트 검증
        verify(templateMapper, times(1)).updateExercise(argThat(ex -> ex.getTplAttrSeq().equals(501L)));

        // 3. 신규 항목 삽입 및 관계 연결 검증
        verify(templateMapper, times(1)).insertExercise(argThat(ex -> ex.getTplAttrSeq() == null));
        verify(templateMapper, times(1)).insertRelation(eq(tplSeq), any());

        // 4. 제외된 항목(502L) Soft Delete 검증
        verify(templateMapper, times(1)).deleteExerciseBySeq(502L);
    }

    @Test
    @DisplayName("템플릿 삭제 테스트")
    void deleteTemplate_Test() {
        // given
        Long tplSeq = 200L;
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("deleteUser");
        when(request.getRemoteAddr()).thenReturn("2.2.2.2");

        // when
        templateService.deleteTemplate(tplSeq, request);

        // then
        verify(templateMapper, times(1)).deleteTemplate(any(TemplateDto.class));
        verify(templateMapper, times(1)).deleteRelationByTplSeq(tplSeq);
        verify(templateMapper, times(1)).deleteExerciseByTplSeq(any(TemplateDto.class));
    }
}
