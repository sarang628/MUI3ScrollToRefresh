# Modifier.nestedScroll 이해하기

compose.ui 에서 제공하는 기능

## NestedScrollConnection
package androidx.compose.ui.input.nestedscroll.NestedScrollConnection

nested scroll 시스템과 연결을 인터페이스 하는 클래스

어떻게?

- 이 connection 클래스를 nested scroll 구조에 nested scroll modifier에 통과 시킨다.
- 스크롤 되는 주체(scrolling child)로 부터 스크롤 이벤트들이 연결 된다.
- 이제 스크롤 이벤트를 받을 수 있다.

