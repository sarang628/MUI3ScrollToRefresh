package com.sryang.mui3scrolltorefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun NestedScrollConnectionPractice() {
    var onPreScroll by remember { mutableStateOf("onPreScroll:") }
    var onPostScroll by remember { mutableStateOf("onPostScroll:") }

    // create a dispatcher to dispatch nested scroll events (participate like a nested scroll child)
    val nestedScrollDispatcher = remember { NestedScrollDispatcher() }

    // create nested scroll connection to react to nested scroll events (participate like a parent)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // we have no fling, so we're interested in the regular post scroll cycle
                // let's try to consume what's left if we need and return the amount consumed
                val vertical = available.y
                //val weConsumed = onNewDelta(vertical)
                //return Offset(x = 0f, y = weConsumed)
                onPostScroll =
                    "onPostScroll:(${consumed.x} ${consumed.y}, ${available.x} ${available.y}, ${source})"
                return super.onPostScroll(consumed, available, source)
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                onPreScroll = "onPostScroll:(${available.x} ${available.y}, ${source})"
                return super.onPreScroll(available, source)
            }
        }
    }

    Column {
        Text("$onPreScroll")
        Text("$onPostScroll")
        Box(
            Modifier
                .size(200.dp)
                .nestedScroll(nestedScrollConnection)
        ) {
            LazyColumn {
                items(1000) {
                    Text("abcd")
                }
            }
        }
    }
}