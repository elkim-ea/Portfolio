// src/pages/Admin/CompanyManagement.tsx
import React, { useState, useEffect, useRef } from 'react';
import { adminApi } from '../../api/adminApi';
import { Company, CompanyCreateRequest } from './AdminTypes';
import Pagination from '../../components/Pagination';

const CompanyManagement: React.FC = () => {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [editingCompany, setEditingCompany] = useState<Company | null>(null);
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);
  const [uploadMethod, setUploadMethod] = useState<'url' | 'upload'>('upload');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [formData, setFormData] = useState<CompanyCreateRequest>({
    companyName: '',
    categories: [''],
    companyLogo: '',
    companyWebsiteUrl: '',
  });
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetchCompanies();
  }, [currentPage, categoryFilter]);

  const fetchCompanies = async () => {
    setLoading(true);
    try {
      const response = await adminApi.companies.getAll(
        { page: currentPage - 1, size: 10 },
        {
          keyword: searchKeyword,
          category: categoryFilter,
        }
      );
      setCompanies(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('기업 조회 실패:', error);
      alert('기업을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setCurrentPage(1);
    fetchCompanies();
  };

  const handleCreate = () => {
    setEditingCompany(null);
    setFormData({
      companyName: '',
      categories: ['LEADER'],
      companyLogo: '',
      companyWebsiteUrl: '',
    });
    setSelectedFile(null);
    setPreviewUrl('');
    setUploadMethod('upload');
    setShowModal(true);
  };

  const handleEdit = (company: Company) => {
    setEditingCompany(company);
    setFormData({
      companyName: company.companyName,
      categories: company.categories || ['LEADER'],
      companyLogo: company.companyLogo || '',
      companyWebsiteUrl: company.companyWebsiteUrl || '',
    });
    setSelectedFile(null);
    setPreviewUrl('');
    setUploadMethod('url');
    setShowModal(true);
  };

  // 카테고리 토글
  const toggleCategory = (category: string) => {
    setFormData(prev => {
      const categories = prev.categories.includes(category)
        ? prev.categories.filter(c => c !== category)
        : [...prev.categories, category];
      
      // 최소 1개는 선택되어야 함
      return {
        ...prev,
        categories: categories.length > 0 ? categories : prev.categories
      };
    });
  };

  // 첫 번째 카테고리만
  const getPrimaryCategory = (categories: string[]) => {
    return categories && categories.length > 0 ? categories[0] : '';
  };

  const handleViewDetail = (company: Company) => {
    setSelectedCompany(company);
    setShowDetailModal(true);
  };

  // 파일 선택 (미리보기만)
  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // 파일 타입 검증
    if (!file.type.startsWith('image/')) {
      alert('이미지 파일만 선택 가능합니다.');
      return;
    }

    // 파일 크기 검증 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('파일 크기는 5MB를 초과할 수 없습니다.');
      return;
    }

    setSelectedFile(file);

    // 미리보기 URL 생성
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleRemoveImage = () => {
    setSelectedFile(null);
    setPreviewUrl('');
    setFormData({ ...formData, companyLogo: '' });
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {

      const dataToSubmit = { ...formData };
        
      let logoFileToSubmit: File | undefined = undefined;

      // 파일 업로드가 선택되었고, 새 파일이 있는 경우
      if (uploadMethod === 'upload' && selectedFile) {
          logoFileToSubmit = selectedFile;
          
      }

      if (editingCompany) {
        await adminApi.companies.update(
                editingCompany.id, 
                dataToSubmit, 
                logoFileToSubmit
            );
        alert('기업이 수정되었습니다.');
      } else {
        await adminApi.companies.create(dataToSubmit, logoFileToSubmit);
        alert('기업이 등록되었습니다.');
      }

      setShowModal(false);
      fetchCompanies();
    } catch (error: any) {
      console.error('기업 저장 실패:', error);
      alert(error.message || '기업 저장에 실패했습니다.');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      await adminApi.companies.delete(id);
      alert('기업이 삭제되었습니다.');
      fetchCompanies();
    } catch (error) {
      console.error('기업 삭제 실패:', error);
      alert('기업 삭제에 실패했습니다.');
    }
  };

  const getCategoryLabel = (category: string) => {
    const labels: Record<string, string> = {
      LEADER: '선도기업',
      SPONSOR: '후원기업',
    };
    return labels[category] || category;
  };

  const getCategoryBadge = (category: string) => {
    const styles: Record<string, string> = {
      LEADER: 'bg-yellow-100 text-yellow-800',
      SPONSOR: 'bg-green-100 text-green-800',
    };
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${styles[category]}`}>
        {getCategoryLabel(category)}
      </span>
    );
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR');
  };

  // 표시할 이미지 URL 결정
  const getDisplayImageUrl = () => {
    if (uploadMethod === 'upload') {
      return previewUrl || formData.companyLogo;
    }
    return formData.companyLogo;
  };

  return (
    <div className="max-w-7xl mx-auto">
      {/* 기존 검색 및 필터, 테이블*/}
      
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">기업 관리</h2>
        <p className="text-gray-600 mt-1">기업을 추가, 수정, 삭제할 수 있습니다.</p>
      </div>

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2">
            <input
              type="text"
              placeholder="기업명 검색"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">전체 분류</option>
            <option value="LEADER">선도기업</option>
            <option value="SPONSOR">후원기업</option>
          </select>
        </div>
        <div className="flex justify-between mt-4">
          <button
            onClick={handleSearch}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            검색
          </button>
          <button
            onClick={handleCreate}
            className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
          >
            + 기업 추가
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기업명</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">분류</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">웹사이트</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">등록일</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">작업</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {companies.map((company) => (
                  <tr 
                    key={company.id} 
                    className="hover:bg-gray-50 cursor-pointer"
                    onClick={() => handleViewDetail(company)}
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-center">{company.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{company.companyName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {/* 여러 카테고리 표시 가능해야함 */}
                      <div className="flex flex-wrap gap-1">
                        {company.categories && company.categories.map((cat, idx) => (
                          <span key={idx}>
                            {getCategoryBadge(cat)}
                          </span>
                        ))}
                        
                      </div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      <div className="max-w-xs truncate">{company.companyWebsiteUrl || '-'}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-center">{formatDate(company.createdAt)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                        onClick={(e) => { e.stopPropagation(); handleEdit(company); }}
                        className="text-blue-600 hover:text-blue-900 mr-4"
                      >
                        수정
                      </button>
                      <button
                        onClick={(e) => { e.stopPropagation(); handleDelete(company.id); }}
                        className="text-red-600 hover:text-red-900"
                      >
                        삭제
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {companies.length === 0 && !loading && (
          <div className="text-center py-12">
            <p className="text-gray-500">기업이 없습니다.</p>
          </div>
        )}
      </div>

      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      {/* 기업 추가/수정 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <h3 className="text-xl font-bold mb-4">
                {editingCompany ? '기업 수정' : '기업 추가'}
              </h3>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    기업명 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.companyName}
                    onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    기업 분류 <span className="text-red-500">*</span>
                  </label>
                  <div className="space-y-2">
                    <label className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.categories.includes('LEADER')}
                        onChange={() => toggleCategory('LEADER')}
                        className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                      />
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                        LEADER
                      </span>
                      <span className="text-sm text-gray-700">선도기업</span>
                    </label>
                    <label className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.categories.includes('SPONSOR')}
                        onChange={() => toggleCategory('SPONSOR')}
                        className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                      />
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                        SPONSOR
                      </span>
                      <span className="text-sm text-gray-700">후원기업</span>
                    </label>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    * 최소 1개 이상 선택해야 합니다
                  </p>
                </div>

                {/* 로고 업로드 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    기업 로고
                  </label>
                  
                  {/* 업로드 방식 선택 */}
                  <div className="flex space-x-4 mb-3">
                    <button
                      type="button"
                      onClick={() => {
                        setUploadMethod('upload');
                        setFormData({ ...formData, companyLogo: '' });
                      }}
                      className={`px-4 py-2 text-sm font-medium rounded-lg ${
                        uploadMethod === 'upload'
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-200 text-gray-700'
                      }`}
                    >
                      파일 선택
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setUploadMethod('url');
                        setSelectedFile(null);
                        setPreviewUrl('');
                      }}
                      className={`px-4 py-2 text-sm font-medium rounded-lg ${
                        uploadMethod === 'url'
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-200 text-gray-700'
                      }`}
                    >
                      URL 직접 입력
                    </button>
                  </div>

                  {uploadMethod === 'upload' ? (
                    /* 파일 선택 (업로드는 저장 시) */
                    <div>
                      <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/*"
                        onChange={handleFileSelect}
                        className="hidden"
                        id="logo-upload"
                      />
                      <label
                        htmlFor="logo-upload"
                        className="flex items-center justify-center w-full px-4 py-3 border-2 border-dashed rounded-lg cursor-pointer transition-colors border-gray-300 hover:border-blue-500 hover:bg-blue-50"
                      >
                        <div className="text-center">
                          <svg
                            className="mx-auto h-12 w-12 text-gray-400"
                            stroke="currentColor"
                            fill="none"
                            viewBox="0 0 48 48"
                          >
                            <path
                              d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02"
                              strokeWidth={2}
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            />
                          </svg>
                          <p className="mt-1 text-sm text-gray-600">
                            {selectedFile ? selectedFile.name : '클릭하여 이미지 선택'}
                          </p>
                          <p className="text-xs text-gray-500">
                            PNG, JPG, GIF (최대 5MB)
                          </p>
                          <p className="text-xs text-blue-600 mt-1">
                            * 실제 업로드는 저장 버튼 클릭 시 진행됩니다
                          </p>
                        </div>
                      </label>
                    </div>
                  ) : (
                    /* URL 직접 입력 */
                    <input
                      type="url"
                      value={formData.companyLogo}
                      onChange={(e) => setFormData({ ...formData, companyLogo: e.target.value })}
                      placeholder="https://example.com/logo.png"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    />
                  )}

                  {/* 이미지 미리보기 */}
                  {getDisplayImageUrl() && (
                    <div className="mt-3 relative inline-block">
                      <div className="border rounded-lg p-2 bg-gray-50">
                        <img
                          src={getDisplayImageUrl()}
                          alt="로고 미리보기"
                          className="h-24 object-contain"
                          onError={(e) => {
                            e.currentTarget.src = 'https://via.placeholder.com/150?text=Invalid+Image';
                          }}
                        />
                      </div>
                      <button
                        type="button"
                        onClick={handleRemoveImage}
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                      {uploadMethod === 'upload' && selectedFile && (
                        <p className="text-xs text-gray-500 mt-1">
                          미리보기 - 저장 시 업로드됩니다
                        </p>
                      )}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    웹사이트 URL
                  </label>
                  <input
                    type="url"
                    value={formData.companyWebsiteUrl}
                    onChange={(e) => setFormData({ ...formData, companyWebsiteUrl: e.target.value })}
                    placeholder="https://www.example.com"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                  >
                    취소
                  </button>
                  <button
                    type="submit"
                    className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700"
                  >
                    {editingCompany ? '수정' : '추가'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* 기업 상세 모달 */}
      {showDetailModal && selectedCompany && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-3xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-start mb-6">
                <h3 className="text-xl font-bold">기업 상세 정보</h3>
                <button
                  onClick={() => setShowDetailModal(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div className="space-y-6">
                {/* 기업 로고 */}
                <div className="flex flex-col items-center bg-gray-50 rounded-lg p-6">
                  {/* <label className="block text-sm font-medium text-gray-700 mb-3">
                    기업 로고
                  </label> */}
                  {selectedCompany.companyLogo ? (
                    <div className="relative">
                      <img 
                        src={selectedCompany.companyLogo} 
                        alt={`${selectedCompany.companyName} 로고`}
                        className="h-64 max-w-xs object-contain border border-gray-200 rounded-lg bg-white p-4"
                        onError={(e) => {
                          // 이미지 로드 실패 시 대체 이미지 표시
                          e.currentTarget.src = 'https://via.placeholder.com/200x100?text=No+Logo';
                          e.currentTarget.alt = '로고 없음';
                        }}
                      />
                      {/* <p className="text-xs text-gray-500 mt-2 text-center">
                        {selectedCompany.companyLogo}
                      </p> */}
                    </div>
                  ) : (
                    <div className="flex flex-col items-center justify-center h-32 w-full border-2 border-dashed border-gray-300 rounded-lg bg-white">
                      <svg 
                        className="h-12 w-12 text-gray-400" 
                        fill="none" 
                        stroke="currentColor" 
                        viewBox="0 0 24 24"
                      >
                        <path 
                          strokeLinecap="round" 
                          strokeLinejoin="round" 
                          strokeWidth={2} 
                          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" 
                        />
                      </svg>
                      <p className="mt-2 text-sm text-gray-500">로고 없음</p>
                    </div>
                  )}
                </div>

                {/* 기업 정보 */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      기업명
                    </label>
                    <p className="text-gray-900 font-semibold text-lg">{selectedCompany.companyName}</p>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      기업 분류
                    </label>
                    <div className="flex flex-wrap gap-2 mt-1">
                      {selectedCompany.categories && selectedCompany.categories.map((cat, idx) => (
                          <span key={idx}>
                            {getCategoryBadge(cat)}
                          </span>
                        ))}
                    </div>
                  </div>
                </div>

                {/* 웹사이트 */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    웹사이트
                  </label>
                  {selectedCompany.companyWebsiteUrl ? (
                    <div className="flex items-center space-x-2">
                      <a 
                        href={selectedCompany.companyWebsiteUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 hover:text-blue-800 hover:underline flex items-center"
                      >
                        {selectedCompany.companyWebsiteUrl}
                        <svg 
                          className="w-4 h-4 ml-1" 
                          fill="none" 
                          stroke="currentColor" 
                          viewBox="0 0 24 24"
                        >
                          <path 
                            strokeLinecap="round" 
                            strokeLinejoin="round" 
                            strokeWidth={2} 
                            d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" 
                          />
                        </svg>
                      </a>
                    </div>
                  ) : (
                    <p className="text-gray-500">-</p>
                  )}
                </div>

                {/* 등록 정보 */}
                <div className="grid grid-cols-2 gap-4 pt-4 border-t">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      등록일
                    </label>
                    <p className="text-gray-900">{formatDate(selectedCompany.createdAt)}</p>
                  </div>
                  
                </div>

                {/* 버튼 */}
                <div className="flex justify-end space-x-3 pt-4 border-t">
                  <button
                    onClick={() => {
                      setShowDetailModal(false);
                      handleEdit(selectedCompany);
                    }}
                    className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => setShowDetailModal(false)}
                    className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
                  >
                    닫기
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CompanyManagement;