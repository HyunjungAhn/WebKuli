# Android 가이드
 * 안드로이드 native/hybrid 앱, 모바일웹 가이드
 * 툴 세팅 방법은 세팅 가이드 참고

## Fixture 추가 및 드라이버 선택
 * 스크립트 작성 시, 아래와 같이 WebKuliFixture를 추가하고 타깃시스템을 android 로 선택한다.
 * SetUp 페이지를 생성하여 Fixture를 추가해 두면, 테스트 페이지에서는 별도로 추가하지 않아도 된다.

```
!|com.nhncorp.ntaf.Import|
|com.nts.ti|

!|WebKuliFixture|

|select driver|android|
```

## 1단계::디바이스 연결 확인
### 1. PC에 테스트 디바이스를 USB 케이블로 연결한다.
 * PC에 해당 디바이스에 대한 USB드라이버가 설치되어 있어야 한다.(제조사 사이트 참고)
삼성 통합USB드라이버 설치 http://local.sec.samsung.com/comLocal/support/down/kies_main.do?kind=usb
LG USB Driver for Android 설치 http://www.lgmobile.co.kr/lgmobile/front/download/retrieveDownloadMain.dev
팬텍 모델별 USB드라이버 설치 http://www.pantechservice.co.kr/down/software/usb.sky
Google OEM USB Drivers http://developer.android.com/tools/extras/oem-usb.html
 * 디바이스의 환경설정 > 개발자 옵션 메뉴에서 개발자 옵션 및 USB 디버깅이 활성화되어 있어야 한다.(디바이스 내 메뉴는 기종마다 다를 수 있음)
 * Android SDK 설치경로의 tools 폴더에서 ddms.bat 실행하거나 명령프롬프트에서 "adb devices" 명령으로 연결 여부를 확인할 수 있다.

### 2. FitNesse에서 device info 키워드를 사용하여 현재 연결되어 있는 디바이스 정보를 확인한다.
스크립트

```
|show|device info|
```

결과
attachment:android_step1.png
 * 정보 : 시리얼, 제조사, 모델명, 통신사, 네트워크, IP, SDK버전, OS버전
 * 디바이스 관련 키워드 이용 시, 시리얼 또는 IP 정보가 필요함.

## 2단계::서버/테스트앱 설치
 * 모바일웹의 경우, 본 단계 수행하지 않음.

### 1. PC에 테스트 앱(.apk) 파일을 다운로드한다.

### 2. 서버/테스트앱 설치 스크립트 작성
1) FitNesse에 임의 테스트 페이지를 생성한다.
2) 테스트 스크립트 작성
 * 시스템 환경변수에 JAVA_HOME과 ANDROID_HOME을 설정하지 않은 경우에는 스크립트 상에서 아래와 같이 경로를 지정해야 한다.

```
|setEnv|JAVA_HOME|C:\Java\jdk1.7.0_21|
|setEnv|ANDROID_HOME|C:\adt-bundle-windows-x86-20130522\sdk|
```
 * launch app 키워드를 사용하여 테스트 앱(.apk) 설치

```
|launch app|디바이스 시리얼|테스트 앱(apk)|
```
※ 가운데 칸에는 device info의 시리얼을, 마지막 칸에는 테스트 앱(.apk)을 다운로드한 폴더의 절대경로를 넣어준다.
3) 스크립트 저장 후 좌측 "Test" 메뉴를 클릭하여 Test를 실행한다.

### 3. 테스트 앱이 자동으로 실행되면 ok!!
 * 단말기에 테스트 앱 설치가 완료되면 해당 앱이 자동으로 실행된다.

## 3단계::테스트 대상 디바이스 등록

### 1. 앱/하이브리드
 * 테스트를 실행할 디바이스가 WebKuli에 필수로 추가되어 있어야 하며, 방법은 아래와 같다.

1) Edit 모드에서 add device 키워드를 사용하여 테스트 대상 기기를 등록한다.

```
|add device|디바이스 시리얼|호스트|포트번호|
```
※ 두 번째 칸에는 device info의 시리얼을, 세 번째 칸에는 ip를, 마지막 칸에는 포트번호를 임의로 정하여 넣어준다.

2) 스크립트 저장 후 좌측의 "Test" 메뉴를 클릭하여 테스트를 실행한다.
3) 디바이스 등록이 완료되면, http://호스트:포트번호/inspector 로 접근 시, Selendroid Inspector Documentation 화면이 노출된다.

### 2. 모바일웹
1) Edit 모드에서 open browser 키워드를 사용하여 스크립트를 작성한다.

```
|open browser|디바이스 시리얼|localhost|8080|열고자 하는 주소|
```
2) 스크립트 저장 후 좌측의 "Test" 메뉴를 클릭하여 테스트를 실행한다.
3) Android WebDriver가 실행되고, 지정한 주소로 웹 화면이 띄워진다.

## 4단계::Element 식별 방법
 * Element는 id, tag name, value 등의 유니크한 값으로 식별하며, 아래와 같은 방법으로 그 값을 알 수 있다.

### 1. print element 키워드 이용
 * tag, id, value(이상 인덱스 포함), 좌표 정보를 알 수 있다.
스크립트

```
|show|print element|
```
결과

```
||show||print element||TextView[0]::btn_quick_home[0]::홈[0](111,293),(133x174)
TextView[1]::btn_quick_top100[0]::TOP 100[0](293,293),(133x174)
TextView[2]::btn_quick_album[0]::최신앨범[0](475,293),(133x174)
TextView[3]::btn_quick_recommand[0]::추천음악[0](111,496),(133x174)
TextView[4]::btn_quick_genre[0]::장르별음악[0](293,496),(133x174)
TextView[5]::btn_quick_music_video[0]::뮤직비디오[0](475,496),(133x174)
TextView[6]::btn_quick_my_music[0]::마이뮤직[0](111,699),(133x174)
TextView[7]::btn_quick_search[0]::검색[0](293,699),(133x174)
TextView[8]::btn_quick_setting[0]::설정[0](475,699),(133x174)
View[0]::banner_click_holder[0]::-::(111,896),(497x141)
||
```
ex) TextView[2]::btn_quick_album[0]::최신앨범[0](475,293),(133x174)
 * TextView[2] : tag name
 * btn_quick_album[0] : id
 * 최신앨범[0] : value
 * (475,293) : 좌표
 * (133x174) : 영역사이즈

※ [] 안의 숫자는 인덱스를 표시한 것으로 '0'일 경우, 인덱스를 지정해주지 않아도 되지만, 1 이상일 경우에는 키워드 사용 시 지정해 주어야 한다.

ex> |click|TextView|2|

### 2. Selendroid Inspector Documentation 이용(http://호스트:포트번호/inspector) 
 * 좌측에는 테스트 앱 현재화면을 스크린샷으로 표시하고, 우측에는 UI 구성요소를 트리 형태로 제공한다.
 * 각 element의 상세정보에서 tag, name, id, value 정보 등을 알 수 있으며, 스크린샷에서 element 선택 시, XPath 형태로 스크립트를 자동으로 만들어주는 기능도 제공한다.
 * 웹뷰는 지원하지 않는다.

### 3. uiautomatorview.bat 이용(\Android\android-sdk\tools)
 * 안드로이드에서 제공하는 툴이며, tag, value, 좌표 정보를 알 수 있다.
 * 웹뷰는 지원하지 않는다.

## 5단계::키워드 등록 및 Resource 로딩
 * id, tag name, value 등의 element를 그대로 사용하면 스크립트의 가독성을 저하시키는데, 이러한 문제점은 element를 키워드로 만들어주면 해소된다. 그렇게 되면, 스크립트를 쉽고 빠르게 작성할 수 있고, 작성자가 아니더라도 스크립트를 이해하고 유지보수하는 작업이 쉬워진다.

### 1. 키워드 등록
insert keyword 를 사용하여 resource 파일에 키워드를 등록한다.

```
|insert keyword|N드라이브앱_홈|홈.로고이미지|home_logo_image|
```
※ 두 번째 칸에는 저장할 resource 파일명을 입력하고, 세 번째 칸에는 element 대신 사용할 키워드를 입력하고, 네 번째 칸에는 element를 입력한다.
※ 스크립트 저장 후 "Test" 실행

### 2. Resource 로딩 
 * 테스트 페이지에서 element의 키워드를 사용하기 위해서는 resource 파일을 로딩하는 과정이 필요하다.
 * load resource 를 사용하여 resource 파일을 로딩한다.

```
|load resource|N드라이브앱_홈|
```

※ 두 번째 칸에는 resource 파일의 이름을 입력한다.

## 6단계::테스트 작성 예시
 * 1 ~ 5 단계 완료되었다면, 이제 테스트를 수행할 수 있다.
 * 앱 테스트

```
!|com.nhncorp.ntaf.Import|
|com.nts.ti|

!|WebKuliFixture|

|load resource|리소스 파일명|

|add device|테스트 기기 시리얼넘버|호스트|포트번호|

|click|리소스 키워드 혹은 엘리먼트(tag, id, value 등)|

|check|exists|리소스 키워드 혹은 엘리먼트(tag, id, value 등)|
```

 * 웹 테스트

```
!|com.nhncorp.ntaf.Import|
|com.nts.ti|

!|WebKuliFixture|

|load resource|리소스 파일명|

|open browser|테스트 기기 시리얼넘버|호스트|포트번호(8080)|

|click|리소스 키워드 혹은 엘리먼트(xpath)|

|check|exists|리소스 키워드 혹은 엘리먼트(xpath)|
```
