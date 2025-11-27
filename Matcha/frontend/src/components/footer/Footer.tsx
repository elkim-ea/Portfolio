import React from "react";
import googleDriveIcon from "@/assets/urlimage/구글드라이브.png";
import githubIcon from "@/assets/urlimage/깃.png";

const Footer: React.FC = () => {
  return (
    <footer
      className="relative w-full bg-[#FAF3E0] border-t border-[#E8F5E9]
                 text-[#2E7D32] py-8 px-6 text-center text-sm
                 shadow-[0_-2px_5px_rgba(0,0,0,0.05)]"
    >
      {/* ✅ 기본 텍스트 정보 */}
      <div>
        <p>
          상호명: <b>Matcha World</b> | 사업자등록번호: 120-05-00075 | 대표자: MATCHA <br />
          주소: 서울 강남구 강남대로94길 20 삼오빌딩 5-9층 | 고객 지원: 080-202-5103 (평일 10:00~18:00)
          <br />
          메일문의:{" "}
          <a
            href="mailto:matcha@gmail.com"
            className="text-[#1565C0] hover:text-[#42A5F5] transition"
          >
            matcha@gmail.com
          </a>
        </p>
        <p className="text-xs text-[#1565C0] mt-2">
          © {new Date().getFullYear()} Matcha World. All rights reserved.
        </p>
      </div>

      {/* ✅ 오른쪽 하단 링크 아이콘 (구글 시트 + 깃허브) */}
      <div className="absolute right-6 bottom-4 flex space-x-4">
        {/* Google Sheets 링크 */}
        <a
          href="https://docs.google.com/spreadsheets/d/14PL2Na9Bg71wdEgNAwD2eA00XNIM1_2ZhkZkQd8pfek/edit?gid=0#gid=0"
          target="_blank"
          rel="noopener noreferrer"
          className="hover:opacity-80 transition-opacity"
        >
          <img
            src={googleDriveIcon}
            alt="Google Drive"
            className="w-7 h-7 object-contain"
          />
        </a>

        {/* GitHub 링크 */}
        <a
          href="https://github.com/SJF02/Matcha"
          target="_blank"
          rel="noopener noreferrer"
          className="hover:opacity-80 transition-opacity"
        >
          <img
            src={githubIcon}
            alt="GitHub"
            className="w-7 h-7 object-contain"
          />
        </a>
      </div>
    </footer>
  );
};

export default Footer;