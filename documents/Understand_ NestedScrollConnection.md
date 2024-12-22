# Modifier.nestedScroll 이해하기

compose.ui 에서 제공하는 기능

## NestedScrollConnection

package androidx.compose.ui.input.nestedscroll.NestedScrollConnection

nested scroll 시스템과 연결을 인터페이스 하는 클래스

어떻게?

- 이 connection 클래스를 nested scroll 구조에 nested scroll modifier에 통과 시킨다.
- 스크롤 되는 주체(scrolling child)로 부터 스크롤 이벤트들이 연결 된다.
- 이제 스크롤 이벤트를 받을 수 있다.

## 주요 함수

nested scroll은 부모와 자식둘다 스크롤이 가능한데 겹쳐져있는 경우이다.

### onPreScroll()

자식이 스크롤이 된다면 onPreScroll 에서 움직이는 수치들이 들어온다.

### onPreScroll()

부모가 스크롤 된다면 onPostScroll 에서 움직이는 수치들이 들어온다.