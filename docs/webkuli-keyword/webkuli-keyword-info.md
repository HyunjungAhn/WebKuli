# WebKuli Keyword Info

## A
### accept confirm
 * 지원 : web
 * Confirm 창의 확인 또는 예 버튼 클릭

```
|accept confirm|
```
 
### adb screen shot
 * 지원 : Android
 * adb 명령어를 사용하여 디바이스의 현재 화면을 캡쳐 및 저장하고, 출력을 위한 HTML태그를 가져옴
 
 ```
|adb screen shot|serial|fileName|

serial : 디바이스 시리얼 (생략 가능)
fileName : 저장 할 파일명
```
 * Example

```
adb screen shot|42f00714ea7d5f25|웹툰_화_목록|
```

### add device
 * 지원 : Android
 * 테스트 디바이스 등록
 * 여러 개의 디바이스를 연결할 경우, 해당 디바이스마다 addDevice를 해주고 포트번호를 다르게 설정

```
|add device|serial|host|port|

serial : 디바이스 시리얼
host: 원격 디바이스의 호스트
port : 원격 디바이스 포트를 포워딩할 로컬 포트번호
```
 * Example

```
|add device|4300c80020a9300f|localhost|9090|
```

## C
### capture off
 * 지원 : 공통
 * 키워드 수행 전/후로 화면을 자동으로 캡쳐하는 기능 비활성화
 * 자동 캡쳐 기능은 기본적으로 off 설정되어 있음

```
|capture off|
```

### capture on
 * 지원 : 공통
 * 키워드 수행 전/후로 화면을 자동으로 캡쳐하는 기능 비활성화
 * 자동 캡쳐 기능은 기본적으로 off 설정되어 있음

```
|capture on|
```

### cancel confirm
 * 지원 : web
 * Confirm창의 취소 또는 아니오 클릭

```
|cancel confirm|
```

### clear app data
 * 지원 : Android
 * 앱의 Data와 Cache 삭제

```
|clearAppData|serial|apkPath|

serial : 디바이스 시리얼
apkPath : Data와 Cache를 삭제하려는 apk의 fullPath
```
 * Example

```
clearAppData|42f00714ea7d5f25|!-C:\WebKuli\apk\webtoon\WebToon_1.4.4_REAL_20131112.apk-!|
```

### clear text
 * 지원 : 공통
 * 해당 index의 element(EditText)의 text(value)를 지워줌

```
|clearText|resourceName|index|

resourceName : 키워드 리소스 혹은 element의 id/value/tag name
index : 해당 element에 대한 index
```
 * Example

```
ㅣclearText|common_input_edit|1|
```

### clear popup
 * 지원 : web
 * 비정상 종료된 팝업 닫기

```
|clearPopup|
```

### click
 * 지원 : 공통
 * 해당 index의 element를 click

```
|click|resourceName|index|

resourceName : element의 id, value 또는 tag name
index : 해당 element에 대한 index
```
 * Example

```
ㅣclick|file_explore_item_checkbox|1|
```

### click at
 * 지원 : 공통
 * offset을 반영한 위치에 click event 발생

```
|click at|resourceName|index|

resourceName : 키워드 리소스 혹은 element의 id/value/tag name
index : 해당 element에 대한 index
```
 * Example

```
ㅣclick at|file_explore_item_checkbox|1|
```

### close
 * 지원 : 공통
 * 드라이버 종료

```
|close|
```

### close alert
 * 지원 : web
 * Alert의 확인을 클릭 함 

```
|close alert|
```

### close browser
 * 지원 : web
 * 브라우저 종료

```
|close browser|
```

### count elements
 * 지원 : web
 * 입력받은 XPath에 해당하는 element 개수 반환

```
|count element|xPath|

xPath : 해당 element에 대한 XPath
```
 * Example

```
|show|count element|//*[@id='fmenu']|
```

## D
### device info
 * 지원 : Android
 * 디바이스 정보 출력

```
|show|device info|
```

## E
### encrypt string 
 * 지원 : web
 * 입력받은 문자열을 암호화하여 반환

```
|encrypt string|text|

text : 암호화할 문자열
```
 * Example

```
|show|encrypt string|abc123|
```

### exists
 * 지원 : 공통
 * 해당 index의 element가 화면에 존재하는지 확인

```
|exists|resourceName|index|

resourceName : element의 id, value 또는 tag name (web의 경우 XPath)
index : 해당 element에 대한 index
```
 * Returns

```
boolean : 존재하면 true, 아니면 false
```
 * Example

```
|exists|navigation_second_layout|2|
```

### exists text
 * 지원 : web
 * 해당 문자열이 웹 페이지 내 존재하는지 확인

```
|exists text|text|

text : 확인하려는 문자열
```
 * Returns

```
boolean : 존재하면 true, 아니면 false
```
 * Example

```
|exists text|날씨|
```

## F
### flick
 * 지원 : 공통
 * 해당 index의 element를 기준으로 direction 방향으로 flick

```
|flick|resourceName|index|direction|

resourceName : element의 id, value 또는 tag name
index : element의 인덱스 
direction : 방향 - 왼쪽/-, 오른쪽/+
```
 * Example

```
|flick|photo_view_layout|0|-300|
```

### forward
 * 지원 : Android
 * 디바이스의 테스트 앱과 연결할 port를 지정

```
|forward|serial|port|

serial : 디바이스 시리얼
port : 포트
```
 * Example

```
|forward|42f6af1c91b8bf23|8083|
```

## G
### get alert text
 * 지원 : web
 * 노출된 Alert창의 내용을 가져옴

```
|get alert text|
```
 * Example

```
|check|get alert text|기대결과|
```

### get current url
 * 지원 : Android, web
 * 현재 URL 정보를 가져옴

```
|get current url|
```
 * Example

```
|check|get current url|기대결과|
```

### get device list
 * 지원 : Android
 * 연결 가능한 디바이스 리스트를 출력

```
|show|get device list|
```

### get element count
 * 지원 : 공통
 * 해당 element의 개수 반환

```
|get element count|resourceName|

resourceName : element의 id, value 또는 tag name (web의 경우 XPath)
```
 * Example

```
|show|get element count|//*[@id='fmenu']|
```

### get location
 * 지원 : Android
 * 해당 element의 위치 좌표(x, y)를 반환

```
|get location|resourceName|

resourceName : element의 id, value 또는 tag name
```
 * Example

```
|show|get location|extends_button|
```

### get page source
 * 지원 : web
 * 현재 웹 페이지의 HTML소스를 출력

```
|show|get page source|
```

### get text
 * 지원 : 공통
 * 해당 index의 element의 text(value)를 반환

```
|get text|resourceName|index|

resourceName : element의 id, value 또는 tag name (web의 경우 XPath)
index : 해당 element에 대한 index
```
 * Example

```
|show|get text|folder_explore_item_filename_text|1|
```

### go back
 * 지원 : Android, web
 * 이전 페이지로 이동

```
|go back|
```

### go forward
 * 지원 : Android, web
 * 다음 페이지로 이동

```
|go forward|
```

### go to url
 * 지원 : Android, web
 * 해당 URL 페이지로 이동

```
|go to url|url|

url : 이동하려는 페이지의 URL
```
 * Example

```
|go to url|http://www.naver.com/|
```

## H
### hasText
 * 지원 : Android
 * 입력된 문자열 존재 여부 확인

```
|has text|compareText|

compareText : 확인하려는 문자열
```
 * Example

```
|has text|영문뉴스듣기|
```

## I
### insert keyword
 * 지원 : 공통
 * tag name이나 id를 원하는 키워드로 지정

```
|insert keyword|resource|key|value|

resource : 생성되는 properties 파일의 이름
key : 원하는 키워드
value : 지정하려는 tag name 또는 id
```
 * Returns

```
이미 존재 시 "key::(value) already exists!", 
value 변경 시 "key::(old_value) is changed to (new_value)!", 
새로 추가 시 "key::(value) is added!"
```
 * Example

```
|insert keyword|N드라이브앱_홈|홈.탐색기|home_explorer_image|
```

### instrument
 * 지원 : Android
 * 서버와 테스트 하려는 앱을 연결하여 테스트가 가능하도록 함

 ```
 |instrument|serial|appPath|port|

serial : 디바이스 시리얼
appPath : 테스트 앱의 절대 경로
port : 디바이스와 연결할 포트
```
 * Example

```
|instrument|42f6af1c91b8bf23|D:\apk\Ndrive_Android_2013-07-23_08_32_53.apk|
```

### is disappear
 * 지원 : Android
 * 입력된 문자열 존재 여부 확인

```
|is disappear|compareText|

compareText : 확인하려는 문자열
```
 * Returns

```
boolean : 존재하면 true, 아니면 false
```
 * Example

```
|is disappear|영문뉴스듣기|
```

### is ready
 * 지원 : Android
 * 테스트 앱이 selendroid server와 연결된 상태인지 확인

```
|show|is ready|
```
 * Returns

```
boolean : 연결된 상태이면 true, 아니면 false
```

## L
### launch app
 * 지원 : Android
 * 연결된 디바이스에 selendroid-server-0.5.0.apk와 입력한 apk를 올려주고, instrument와 port forward까지 자동으로 수행

```
|launch app|serial|appPath|

serial : 디바이스 시리얼 
appPath : 설치하려는 apk의 full path
```
 * Example

```
|launch app|42f6af1c91b8bf23|D:\apk\Ndrive_Android_2013-07-23_08_32_53.apk|
```

### load resource
 * 지원 : 공통
 * .properties 파일을 load

```
|load resource|resource|

resource : properties 파일 이름
```
 * Example

```
|load resource|N드라이브앱_홈|
```

### long tap
 * 지원 : Android
 * 해당 index의 element를 길게 탭

```
|long tap|resourceName|

resourceName : element의 id, value 또는 tag name
```
 * Example

```
|long tap|file_storage_thumbnail_image|
```

## O
### open browser
 * 지원 : 공통
 * 해당 URL로 웹 브라우저를 open 함

```
|open browser|url|

url : 웹 브라우저를 열었을 때 이동하려는 페이지의 URL
```
 * Example

```
|open browser|http://www.naver.com/|
```

## P
### print cookies
 * 지원 : web
 * 쿠키 정보를 출력

```
|show|print cookies|
```

### print element
 * 지원 : Android
 * 현재 화면에서 show 상태의 element들을 출력

```
|show|print element|
```
 * Returns

```
현재 화면에서 show 상태인 element의 tag name, id, value 값
```

## R
### reclaim app
 * 지원 : Android
 * 연결된 디바이스에 selendroid server와 instrument 되어 있는 앱을 삭제

```
|reclaim app|serial|

serial : 디바이스 시리얼
```
 * Example

```
|reclaim app|42f6af1c91b8bf23|
```

## S
### screen shot
 * 지원 : 공통
 * 디바이스 또는 웹 페이지의 현재 화면을 캡쳐하여 저장하고, 출력을 위한 HTML태그를 가져옴

```
|screen shot|message|

message : 저장 할 파일명
```
 * Example

```
|screen shot|탐색기_삭제_전|
```

### secureInputText
 * 지원 : web
 * 해당 element에 암호화된 문자열을 입력

```
|secure input text|resourceName|keys|

resourceName : 키워드 리소스 혹은 element의 XPath
keys : 암호화된 문자열 ({{{[encryptString]}}} 사용)
```
 * Example

```
|secure input text|//*[@id='search_bar_keyword']|cN+brFzvKJR9eoYGzB8r1w==|
```

### scroll
 * 지원 : Android
 * 해당 element의 index를 기준으로 화면이 direction 방향으로 움직이게 스크롤

```
|scroll|resourceName|index|direction|

resourceName : element의 id, value 또는 tag name
index : element의 인텍스
direction : 방향 - 아래/-, 위/+
```
 * Example

```
|scroll|탐색기.항목.이름|3|up|
```

### select
 * 지원 : Android, web
 * 해당 셀렉트박스에서 입력받은 인덱스로 옵션을 선택

```
|select|resourceName|optIdx|

resourceName : 해당 셀렉트박스의 XPath 경로
optIdx : 선택하려는 element의 index
```
 * Example

```
|select|//*[@id='gradeSelect']|2|
```

### select value
 * 지원 : web
 * 해당 셀렉트박스에서 값으로 옵션을 선택

```
|select value|resourceName|index|optValue|

resourceName : 해당 셀렉트박스의 XPath 경로
index : 셀렉트박스의 인덱스번호 
optValue : 셀렉트박스 내 옵션의 값
```
 * Example

```
|select value|//*[@id='gradeSelect']|0|나무등급|
```

### send key
 * 지원 : 공통
 * 해당 인덱스의 element(EditText)에 값을 입력

```
|send key|resourceName|index|keys|

resourceName : element의 id, value 또는 tag name (web의 경우 XPath)
index : 해당 element에 대한 index 
keys : 입력하려는 문자열
```
 * Example

```
ㅣsend key|common_input_edit|1|abced|
```

### set browser
 * 지원 : web
 * local Test시 Test할 browser 선택

```
|set browser|browserName|

browserName : 브라우저 명
```
 * Example

```
|set browser|ie|
```

### set env
 * 지원 : 공통
 * 환경 변수 값 등록 시 이용 (JAVA_HOME, ANDROID_HOME, -D옵션으로 추가되는 환경 변수를 추가할 수 있음)

```
|set env|envKey|envValue|

envKey : JAVA_HOME 또는 ANDROID_HOME 
envValue : envKey로 설정하려는 값
```
 * Example

```
|set env|JAVA_HOME|C:\jdk1.7.0_21|
|set env|ANDROID_HOME|C:\adt-bundle-windows-x86-20130522\sdk|
```

### set orientation
 * 지원 : Android
 * 디바이스의 화면 회전방향을 설정

```
|set orientation|orientation|

orientation 방향 (portrait 세로/ landscape 가로)
```
 * Example

```
|set orientation|portrait|
```

### set resouce path
 * 지원 : web
 * 웹 브라우저 드라이버가 설치된 경로를 지정함

```
|set resource path|resourcePath|

resourcePath : 웹 브라우저 드라이버가 설치된 경로
```
 * Example

```
|set resource path|C:\WebDriver|
```

### set retry interval
 * 지원: 공통
 * element 찾기 실패시 다음 시도까지의 시간 설정 (dafault 500ms)

```
|set retry interval|retryInterval|

retryInterval : element 찾기 실패시 다음 시도까지의 시간(단위:ms)
```
 * Example

```
|set retry interval|500|
```

### set time out
 * 지원 : 공통
 * element를 찾는 최대시간 설정 (dafault 3000ms)

```
|set time out|timeOut|

timeOut : element를 찾는 최대시간(단위:ms)
```
 * Example

```
|sett ime out|3000|
```

### single tap
 * 지원 : Android
 * 해당 index의 element의 중점을 한번 탭

```
|single tap|resourceName|index|

resourceName : 키워드 리소스 혹은 element의 id/value/tag name
index : 해당 element에 대한 index
```
 * Example

```
|single tap|file_storage_thumbnail_image|3|
```

### start app
 * 지원 : Android
 * 지정한 Path에 존재하는 앱 실행
 * 기기에 해당 앱이 설치되어 있는 상태에서 수행 가능

```
|start app|serial|targetApkPath|

serial : 테스트 디바이스의 시리얼
targetApkPath : 실행시키려는 앱의 full path
```
 * Example

```
|start app|42f6af1c91b8bf23|D:\apk\Ndrive_Android_2013-07-23_08_32_53.apk|
```

### switch device
 * 지원 : Android
 * 테스트 디바이스 변경
 * 본 키워드 사용 전에 addDevice로 해당 디바이스가 등록되어 있어야 함

```
|switch device|serial|

serial : 변경하려는 디바이스의 시리얼
```
 * Example

```
|switch device|4300c80020a9300f|
```

## U
### update app
 * 지원 : Android
 * 이미 설치되어 있는 앱을 삭제하지 않고, 입력한 apk로 업데이트

```
|update app|serial|apkPath|

serial : 디바이스 시리얼
apkPath : 업데이트 하려는 apk의 fullPath
```
 * Example

```
|update app|42f6af1c91b8bf23|D:\apk\Ndrive_Android_2013-07-23_08_32_53.apk|
```