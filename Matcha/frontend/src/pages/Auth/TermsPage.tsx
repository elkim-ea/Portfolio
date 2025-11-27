// src/pages/TermsPage.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authApi, Terms } from '../../api/authApi';

const TermsPage: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [terms, setTerms] = useState<Terms[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchTerms();
    }, []);

    const fetchTerms = async () => {
        try {
            const termsData = await authApi.getAllTerms();
            setTerms(termsData);
        } catch (error: any) {
            console.error('약관 조회 실패:', error);
            alert(error.message || '약관을 불러오는데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const handleGoBack = (): void => {
        // location.state가 있으면 해당 state를 유지하면서 이전 페이지로 이동
        const state = location.state as any;
        
        if (state?.from) {
            // from 경로로 이동하면서 formData를 다시 전달
            navigate(state.from, { state });
        } else {
            // state가 없으면 단순히 뒤로가기
            navigate(-1);
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-white-green">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-main-green"></div>
            </div>
        );
    }

    return (
        // 배경: sub-ivory 유지, 화면 중앙에 내용 배치
        <div className="flex items-center justify-center min-h-screen bg-white-green p-4">
            <div className="w-full max-w-2xl p-8 space-y-6 bg-sub-ivory rounded-lg shadow-lg">
                
                <h2 className="text-2xl font-bold text-center text-gray-900 border-b pb-3 mb-4">
                    서비스 이용 약관
                </h2>
                
                {/* 약관 본문 컨테이너 */}
                <div className="h-96 overflow-y-auto p-4 border border-gray-200 rounded-md bg-sub-ivory text-gray-700 text-sm space-y-6">
                    {terms.length === 0 ? (
                        <p className="text-center text-gray-500">표시할 약관이 없습니다.</p>
                    ) : (
                        terms.map((term) => (
                            <div key={term.id} className="space-y-3">
                                <div className="border-b pb-2">
                                    <h3 className="text-lg font-semibold text-gray-800">
                                        {term.title}
                                        {term.isRequired && (
                                            <span className="ml-2 text-xs text-red-600">(필수)</span>
                                        )}
                                    </h3>
                                    <p className="text-xs text-gray-500">버전: {term.version}</p>
                                </div>
                                <div 
                                    className="whitespace-pre-wrap"
                                    dangerouslySetInnerHTML={{ __html: term.content }}
                                />
                            </div>
                        ))
                    )}
                </div>
                
                {/* 뒤로가기 버튼 */}
                <div className="flex justify-center pt-4">
                    <button
                        onClick={handleGoBack}
                        className="py-2 px-8 border border-transparent rounded-md shadow-sm text-base font-medium text-white bg-main-green hover:bg-sub-green focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-main-green transition-colors"
                    >
                        확인하고 돌아가기
                    </button>
                </div>
                
            </div>
        </div>
    );
};

export default TermsPage;