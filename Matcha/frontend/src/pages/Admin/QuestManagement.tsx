// src/pages/Admin/QuestManagement.tsx
import React, { useState, useEffect } from 'react';
import { adminApi } from '../../api/adminApi';
import { Quest, QuestCreateRequest } from './AdminTypes';
import Pagination from '../../components/Pagination';

const QuestManagement: React.FC = () => {
  const [quests, setQuests] = useState<Quest[]>([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [authTypeFilter, setAuthTypeFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingQuest, setEditingQuest] = useState<Quest | null>(null);
  const [formData, setFormData] = useState<QuestCreateRequest>({
    title: '',
    description: '',
    category: 'E',
    type: 'DAILY', 
    authType: 'IMAGE',
    rewardScore: 0,
    maxAttempts: 1,
    conditionJson: '{}',
    isActive: true,
    adminId: 1,
  });

  useEffect(() => {
    fetchQuests();
  }, [currentPage, authTypeFilter, typeFilter]);

  const fetchQuests = async () => {
    setLoading(true);
    try {
      const response = await adminApi.quests.getAll(
        { page: currentPage - 1, size: 10 },
        {
          keyword: searchKeyword,
          authType: authTypeFilter,
          type: typeFilter,
        }
      );
      setQuests(response.content);
      setTotalPages(response.totalPages);
    } catch (error) {
      console.error('퀘스트 조회 실패:', error);
      alert('퀘스트를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setCurrentPage(1);
    fetchQuests();
  };

  const handleCreate = () => {
    setEditingQuest(null);
    const userData = localStorage.getItem('user');
    const adminId = userData ? JSON.parse(userData).userId : 1;
    
    setFormData({
      title: '',
      description: '',
      category: 'E',
      type: 'DAILY',
      authType: 'IMAGE',
      rewardScore: 0,
      maxAttempts: 1,
      conditionJson: '{}',
      isActive: true,
      adminId: adminId,
    });
    setShowModal(true);
  };

  const handleEdit = (quest: Quest) => {
    setEditingQuest(quest);
    setFormData({
      title: quest.title,
      description: quest.description,
      category: quest.category,
      type: quest.type,
      authType: quest.authType,
      rewardScore: quest.rewardScore,
      maxAttempts: quest.maxAttempts,
      conditionJson: quest.conditionJson,
      isActive: quest.isActive,
      adminId: quest.adminId,
    });
    setShowModal(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingQuest) {
        await adminApi.quests.update(editingQuest.id, formData);
        alert('퀘스트가 수정되었습니다.');
      } else {
        await adminApi.quests.create(formData);
        alert('퀘스트가 생성되었습니다.');
      }
      setShowModal(false);
      fetchQuests();
    } catch (error) {
      console.error('퀘스트 저장 실패:', error);
      alert('퀘스트 저장에 실패했습니다.');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      await adminApi.quests.delete(id);
      alert('퀘스트가 삭제되었습니다.');
      fetchQuests();
    } catch (error) {
      console.error('퀘스트 삭제 실패:', error);
      alert('퀘스트 삭제에 실패했습니다.');
    }
  };

  const handleToggleActive = async (id: number) => {
    try {
      await adminApi.quests.toggleActive(id);
      fetchQuests();
    } catch (error) {
      console.error('상태 변경 실패:', error);
      alert('상태 변경에 실패했습니다.');
    }
  };

  const getCategoryLabel = (category: string) => {
    const styles: Record<string, string> = {
      E: 'bg-green-100 text-green-800',
      S: 'bg-yellow-100 text-yellow-800',
    };
    const labels: Record<string, string> = {
      E: 'E',
      S: 'S',
    };
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${styles[category]}`}>
        {labels[category]}
      </span>
    );
  };

  const getTypeLabel = (type: string) => {
    const styles: Record<string, string> = {
      DAILY: 'bg-pink-100 text-pink-800',
      WEEKLY: 'bg-blue-100 text-blue-800',
      SEASON: 'bg-orange-100 text-orange-800',
    };
    const labels: Record<string, string> = {
      DAILY: '일일',
      WEEKLY: '주간',
      SEASON: '시즌',
    };
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${styles[type]}`}>
        {labels[type]}
      </span>
    );
  };

  const getAuthTypeLabel = (authType: string) => {
    const styles: Record<string, string> = {
      IMAGE: 'bg-blue-100 text-blue-800',
      TEXT: 'bg-purple-100 text-purple-800',
    };
    const labels: Record<string, string> = {
      IMAGE: '이미지',
      TEXT: '텍스트',
    };
    return (
      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${styles[authType]}`}>
        {labels[authType]}
      </span>
    );
  };

  return (
    <div className="max-w-7xl mx-auto">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-gray-900">퀘스트 관리</h2>
        <p className="text-gray-600 mt-1">퀘스트를 추가, 수정, 삭제할 수 있습니다.</p>
      </div>

      {/* 검색 및 필터 */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="md:col-span-2">
            <input
              type="text"
              placeholder="퀘스트 제목 검색"
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">전체 유형</option>
            <option value="DAILY">일일</option>
            <option value="WEEKLY">주간</option>
            <option value="SEASON">시즌</option>
          </select>
          <select
            value={authTypeFilter}
            onChange={(e) => setAuthTypeFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">전체 인증</option>
            <option value="IMAGE">이미지</option>
            <option value="TEXT">텍스트</option>
          </select>
          {/* <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">전체 상태</option>
            <option value="false">비활성</option>
            <option value="true">활성</option>
          </select> */}
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
            + 퀘스트 추가
          </button>
        </div>
      </div>

      {/* 퀘스트 목록 */}
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
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    제목
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ESG 유형
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    유형
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    인증방식
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    보상점수
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    인증 총 횟수
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    작업
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {quests.map((quest) => (
                  <tr key={quest.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-center">
                      {quest.id}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      <div className="max-w-xs truncate">{quest.title}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-center">
                      {getCategoryLabel(quest.category)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-center">
                      {getTypeLabel(quest.type)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-center">
                      {getAuthTypeLabel(quest.authType)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-center">
                      {quest.rewardScore}점
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-center">
                      {quest.maxAttempts}회
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <button
                        onClick={() => handleToggleActive(quest.id)}
                        className={`px-2 py-1 text-xs font-semibold rounded-full ${
                          quest.isActive
                            ? 'bg-green-100 text-green-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}
                      >
                        {quest.isActive ? '활성' : '비활성'}
                      </button>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center text-sm font-medium">
                      <button
                        onClick={() => handleEdit(quest)}
                        className="text-blue-600 hover:text-blue-900 mr-4"
                      >
                        수정
                      </button>
                      <button
                        onClick={() => handleDelete(quest.id)}
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

        {quests.length === 0 && !loading && (
          <div className="text-center py-12">
            <p className="text-gray-500">퀘스트가 없습니다.</p>
          </div>
        )}
      </div>

      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

      {/* 퀘스트 추가/수정 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <h3 className="text-xl font-bold mb-4">
                {editingQuest ? '퀘스트 수정' : '퀘스트 추가'}
              </h3>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    제목
                  </label>
                  <input
                    type="text"
                    value={formData.title}
                    onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    설명
                  </label>
                  <textarea
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    rows={3}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      ESG 유형
                    </label>
                    <select
                      value={formData.category}
                      onChange={(e) => setFormData({ ...formData, category: e.target.value as any })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="E">E</option>
                      <option value="S">S</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      타입
                    </label>
                    <select
                      value={formData.type}
                      onChange={(e) => setFormData({ ...formData, type: e.target.value as any })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="DAILY">일일</option>
                      <option value="WEEKLY">주간</option>
                      <option value="SEASON">시즌</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      인증 방식
                    </label>
                    <select
                      value={formData.authType}
                      onChange={(e) => setFormData({ ...formData, authType: e.target.value as any })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="IMAGE">이미지</option>
                      <option value="TEXT">텍스트</option>
                    </select>
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      보상 점수
                    </label>
                    <input
                      type="number"
                      value={formData.rewardScore}
                      onChange={(e) => setFormData({ ...formData, rewardScore: parseInt(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      최대 시도 횟수
                    </label>
                    <input
                      type="number"
                      value={formData.maxAttempts}
                      onChange={(e) => setFormData({ ...formData, maxAttempts: parseInt(e.target.value) })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    조건 JSON
                  </label>
                  <textarea
                    value={formData.conditionJson}
                    onChange={(e) => setFormData({ ...formData, conditionJson: e.target.value })}
                    rows={3}
                    placeholder='{"key": "value"}'
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div className="flex items-center">
                  <input
                    type="checkbox"
                    id="isActive"
                    checked={formData.isActive}
                    onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                  />
                  <label htmlFor="isActive" className="ml-2 block text-sm text-gray-900">
                    활성화
                  </label>
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
                    {editingQuest ? '수정' : '추가'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default QuestManagement;