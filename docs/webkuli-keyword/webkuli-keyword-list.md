# WebKuli 키워드 리스트
* 상세 설명 및 예제는 키워드 상세 페이지 참고해주세요.

## 환경설정

| Keyword | Description |
| ------- | ----------- |
|set env|환경 변수 값 등록 시 이용 (JAVA_HOME, ANDROID_HOME, -D옵션으로 추가되는 환경 변수를 추가할 수 있음)|
|set time out|element를 찾는 최대시간 설정 (dafault 3000ms)|
|set retry interval|element 찾기 실패시 다음 시도까지의 시간 설정 (dafault 50ms)|
|capture on|화면 캡쳐(메소드 수행 전/후로 화면을 캡쳐함)를 활성화 함 (default captureOff)|
|capture off|화면 캡쳐(메소드 수행 전/후로 화면을 캡쳐함)를 비활성화 함 (default captureOff)|

## Resource

| Keyword | Description |
| ------- | ----------- |
|load resource|.properties 파일을 load|
|insert keyword|tag name이나 id를 원하는 키워드로 지정|
|print element|현재 화면에서 show 상태의 element들을 print|

## Action
* 동일한 resourceName 존재 시 index를 이용하여 원하는 element를 지정할 수 있습니다. 
* index를 입력하지 않을 경우 default 0으로 세팅됩니다.

| Keyword | Description |
| ------- | ----------- |
|clear text|해당 index의 element(EditText)의 text(value)를 지워줌|
|click|해당 index의 element를 click|
|click at|offset을 반영한 위치에 click event 발생|
|exists|해당 index의 element가 화면에 존재하는지 확인|
|get text|해당 index의 element의 text(value)를 return|
|screen shot|디바이스의 현재 화면을 캡쳐하여 저장하고, 출력을 위한 HTML태그를 가져옴|
|send key|해당 인덱스의 element(EditText)에 값을 입력|
|has text|입력된 문자열 존재 여부 확인|
|select|해당 셀렉트박스에서 입력받은 인덱스로 옵션을 선택|
|select value|해당 셀렉트박스에서 값으로 옵션을 선택|
|single tap|해당 index의 element의 중점을 한번 탭|
|long tap|해당 index의 element를 길게 탭|
|scroll|해당 element의 index를 기준으로 화면이 direction 방향으로 움직이게 스크롤|
|flick|해당 index의 element를 기준으로 direction 방향으로 flick|

## Android
* Android 에서만 제공

### 디바이스
* device info를 이용하여 연결된 device들의 시리얼 정보를 얻을 수 있습니다. 
* PC에 디바이스 1대만 연결된 경우, 시리얼 인자를 넣지 않아도 됩니다. 

| Keyword | Description |
| ------- | ----------- |
|add device|테스트 디바이스 등록|
|switch device|테스트 디바이스를 변경|
|device info|디바이스 정보 출력(모바일 기기가 PC에 USB로 연결된 상태에서만 사용 가능)|

### 앱
* 모바일 기기가 PC에 USB로 연결된 상태에서만 사용 가능합니다.

| Keyword | Description |
| ------- | ----------- |
|launch app|연결된 디바이스에 selendroid-server-0.5.0.apk와 입력한 apk를 올려주고, instrument와 port forward까지 자동으로 수행|
|update app|이미 설치되어있는 앱을 삭제하지 않고, 입력받은 apk로 업데이트|
|instrument|서버와 테스트 하려는 앱을 연결|
|start app|앱을 실행|
|reclaim app|instrument 되어 있는 앱을 삭제|
|clear app data|앱의 Data와 Cache 삭제|
|forward|port를 지정|
|adb screen shot|디바이스의 현재 화면을 캡쳐하여 저장하고, 출력을 위한 HTML태그를 가져옴(adb 사용)|

## 웹브라우저

| Keyword | Description |
| ------- | ----------- |
|open browser|입력받은 URL로 Browser가 열림|
|close browser|브라우저를 닫음|
|get page source|현재 페이지의 HTML소스를 출력|
|print cookies|쿠키 정보를 출력|

## 네비게이션

| Keyword | Description |
| ------- | ----------- |
|go back|이전 페이지로 이동|
|go forward|다음 페이지로 이동|
|go to url|입력한 URL로 이동|

## 팝업/얼럿

| Keyword | Description |
| ------- | ----------- |
|clear popup|popup창을 닫아줌|
|accept confirm|Alert창의 확인 또는 예를 클릭|
|cancel confirm|Alert창의 취소 또는 아니오를 클릭|
|getAlert text|Alert창의 문자열을 가져옴|
|close alert|Alert창의 확인 또는 예를 클릭|

## PC 웹브라우저만

| Keyword | Description |
| ------- | ----------- |
|set browser|테스트할 PC의 host와 Browser 선택|
|set seed|암호화 할 Seed값 설정 (8자 이상)|
|encrypt string|문자열을 암호화|
|secure input text|암호화된 문자열로 element에 입력|
|count elements|페이지 내 존재하는 해당 element의 개수|
|select value|value값으로 select|
|exists text|입력된 문자열이 존재하는지|
