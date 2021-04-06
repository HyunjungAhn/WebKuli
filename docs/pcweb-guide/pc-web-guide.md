# Web Guide
 * PC Web 가이드
 * 툴 세팅 방법은 세팅 가이드 참고

## Fixture 추가 및 드라이버 선택
 * 스크립트 작성 시, 아래와 같이 WebKuliFixture를 추가하고 타깃시스템을 web 으로 선택한다.
 * SetUp 페이지를 생성하여 Fixture를 추가해 두면, 테스트 페이지에서는 별도로 추가하지 않아도 된다.

```
!|com.nhncorp.ntaf.Import|
|com.nts.ti|

!|WebKuliFixture|

|select driver|web|
```

## 1단계::웹 브라우저 띄우기

### 1. 드라이버 설치 및 경로 설정
 * 원하는 브라우저에 해당하는 드라이버 설치가 선행되어야 한다.
 * IE 드라이버
 설명 : https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver
 다운로드 : http://selenium-release.storage.googleapis.com/index.html
 * Chrom 드라이버 : https://sites.google.com/a/chromium.org/chromedriver/downloads
 * firefox는 별도 드라이버 설치 없이 사용 가능
 * 드라이버를 원하는 폴더로 이동시킨 뒤 set driver path 를 이용하여 경로를 설정한다.

```
|set driver path|C:\WebDriver|
```
※ 두 번째 칸에는 드라이버가 저장된 디렉토리 경로를 입력한다.

### 2. 테스트 할 PC의 호스트와 웹 브라우저 선택
 * ie(explorer), cr(chrome), ff(firefox) 가능 
 * local test 시

```
|set browser|ie|
```
※ 두 번째 칸에는 브라우저 명(소문자)을 입력한다.

 * remote test 시
1) http://selenium-release.storage.googleapis.com/index.html 에서 최신버전 selenium-server-standalone-x.x.x.jar 다운로드 
2) cmd 창을 띄운 후 '$cd 디렉토리 명' 을 이용하여 selenium-server-standalone-x.x.x.jar 파일이 있는 폴더로 이동
3) cmd 창에 '$java -jar selenium-server-standalone-x.x.x.jar -role hub' 입력
4) 새로운 cmd 창을 띄운 후, 아래 명령어 입력

```
$java -Dwebdriver.ie.driver="c:\iedriverserver32.exe“(IEDriverServer.exe 파일의 절대경로) -Dwebdriver.chrome.driver="c:\chromedriver.exe“(chromedriver.exe 파일의 절대경로) -jar selenium-server-standalone-2.37.0.jar -role node -hub http://localhost:4444/grid/register'
```
5) 스크립트에 브라우저 명과 host 입력

```
|set browser|ie|10.8.74.200|
```
※ 두 번째 칸에는 브라우저 명(소문자)을 세 번째 칸에는 host를 입력한다.

### 3. 원하는 웹 페이지로 이동

```
|open browser|http://www.naver.com|
```
※ 두 번째 칸에는 웹 페이지 주소를 입력한다.

## 2단계::Element 식별 방법
 * Web Page에서 elements의 XPath를 찾는 방법은 XPath 찾는 법에서 확인할 수 있다.

## 3단계::키워드 등록 및 Resource 로딩
 * element의 XPath를 그대로 사용하면 스크립트의 가독성을 저하시키는데, 이러한 문제점은 element를 키워드로 만들어주면 해소된다. 그렇게 되면, 스크립트를 쉽고 빠르게 작성할 수 있고, 작성자가 아니더라도 스크립트를 이해하고 유지보수하는 작업이 쉬워진다.

### 1. 키워드 등록
insert keyword 를 사용하여 resource 파일에 키워드를 등록한다.

```
|insert keyword|네이버Web_홈|홈.메뉴.웹툰|//*[@id='fmenu']/dd[9]/a/span|
```
※ 두 번째 칸에는 저장할 resource 파일명을 입력하고, 세 번째 칸에는 element 대신 사용할 키워드를 입력하고, 네 번째 칸에는 element를 입력한다.
※ 스크립트 저장 후 "Test" 실행

### 2. Resource 로딩
 * 테스트 페이지에서 element의 키워드를 사용하기 위해서는 resource 파일을 로딩하는 과정이 필요하다.
 * load resource 를 사용하여 resource 파일을 로딩한다.

```
|load resource|네이버Web_홈|
```
※ 두 번째 칸에는 resource 파일의 이름을 입력한다.

## 4단계::테스트 작성 예시
 * 1 ~ 3 단계가 완료되었다면, 이제 테스트를 수행할 수 있다.
 * PC Web 테스트

```
!|com.nhncorp.ntaf.Import|
|com.nts.ti|

!|WebKuliFixture|

|select driver|web|

|load resource|리소스 파일명|

|set resource path|IEDriverServer.exe가 저장된 디렉토리 경로|

|set browser|브라우저 명|

|open browser|웹 페이지 주소|

|check|exists|리소스 키워드 혹은 엘리먼트(xpath)|true|

|click|리소스 키워드 혹은 엘리먼트(xpath)|
```
