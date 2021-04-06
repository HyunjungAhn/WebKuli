# WebKuli 환경 세팅 및 설치

## 1. Java JDK 설치
 * JDK 설치
 * 설치 경로는 C:\Java 권장
 * 시스템 환경변수에 JAVA_HOME 설정

※ 시스템 환경변수 설정 (Windows 기준)
 * 윈도우시작 > 제어판 > 시스템 > 고급 시스템 설정으로 이동 > 환경변수 버튼 클릭
 * 새로 만들기 버튼 클릭 > 변수이름: JAVA_HOME, 변수값: Java를 설치한 디렉토리 경로(예> C:\Java\jdk1.7.0_45) 설정
 * 시스템 변수에서 "Path" 항목 선택 > 편집 버튼 클릭 > ;%JAVA_HOME%\bin 추가 (항목 구분자: ";") > 확인

## 2. Android SDK 설치
 * "DOWNLOAD FOR OTHER PLATFORMS" -> "SDK Tools Only"에서 시스템에 맞는 패키지를 다운로드 받아 설치
 * 설치 경로는 C:\Android 권장
 * 시스템 환경변수에 ANDROID_HOME 설정
 
 ※ 시스템 환경변수 설정 (Windows 기준)
 * 윈도우시작 > 제어판 > 시스템 > 고급 시스템 설정으로 이동 > 환경변수 버튼 클릭
 * 새로 만들기 버튼 클릭 > 변수이름: ANDROID_HOME, 변수값: Android sdk를 설치한 디렉토리 경로(예> C:\Android\sdk) 설정
 * 시스템 변수에서 "Path" 항목 선택 > 편집 버튼 클릭 > ;%ANDROID_HOME%\tools;%ANDROID_HOME%platform-tools 추가 (항목 구분자: ";") > 확인

## 3. WebKuli 실행

### 1) 임의 폴더를 새로 생성 후, WebKuli-1.x.0.jar 파일을 다운로드 한다.

### 2) 명령 프롬프트에서 1)에서 생성한 폴더로 이동 후 아래 명령어를 실행핟다.
`java -jar WebKuli-1.x.0.jar -p 포트번호 `

ex) java -jar WebKuli-1.x.0.jar -p 8082 (8082포트에 실행)
또는 java -jar WebKuli-1.x.0.jar (기본 80포트에 실행)

### 3) Fit 서버가 실행되고, FitNesseRoot 폴더가 자동으로 생성된다.

### 4) 브라우저에서 http://localhost:포트 로 접근한다.
'WELCOME WEBKULI!' 화면이 노출되면 툴 세팅이 모두 완료된 것이다
ex) 8082 포트에 실행한 경우, http://localhost:8082 입력 후 엔터! 기본 포트에 실행한 경우, http://localhost 입력 후 엔터!

## 4. 디바이스 연결
 * 제조사/모델에 해당하는 USB 드라이버를 설치한 후 테스트 기기를 연결한다. (Emulator 사용 시 USB 드라이버 설치는 하지 않아도 된다.)
 * 명령 프롬프트 창에서 아래 명령어를 수행하면 연결된 기기의 serial을 확인할 수 있다.
`adb devices`

