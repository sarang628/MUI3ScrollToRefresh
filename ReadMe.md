# Pull to refresh Module

## how to use

```
@Composable
fun PullToRefreshTest() {
    // pull to refresh 상태 관리
    val state = rememberPullToRefreshState()
    val coroutine = rememberCoroutineScope()

    PullToRefreshLayout(
        pullRefreshLayoutState = state,
        refreshThreshold = 70,
        onRefresh = {
            coroutine.launch {
                delay(1000)
                state.updateState(RefreshIndicatorState.Default)
            }
        }
    ) {
        TestLazyColumn()
    }
}
```

<img src ="./screenshot/a.gif" />

# 어떻게 스크롤 했을 때 화면을 아래로 끌어 당길 수 있는가?

- 프로그레스바(a)와 컨텐츠(b)를 위 아래로(C, Colunm of compose)으로 배치 (PullToRefreshLayout)

- b가 스크롤 최상단에 도달을 감지할 수 있는 장치 마련(PullRefreshNestedScrollConnection)

- b가 최상단에서 스크롤 시 b는 더이상 스크롤 되지 않더라도 '손가락(pointer)으로 계속 swipe down하는 값'(d)을 가져오는 방법(
  PullRefreshNestedScrollConnection)

- d 값 만큼 a의 높이 와 애니메이션을 조정한다.

리프레시 인디케이터 상태는 4가지

```
/**
 * 리프레시 인디케이터 상태
 */
enum class RefreshIndicatorState {
    /** 기본 */
    Default,

    /** 당김 */
    PullingDown,

    /** 이벤트 요청 도달 */
    ReachedThreshold,

    /** 요청 중 */
    Refreshing
}
```

