package id.alpine12.vehiclecare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Pull to refresh wrapper component
 * Provides pull-to-refresh functionality using Material 3 PullToRefreshBox
 *
 * @param isRefreshing Whether the content is currently being refreshed
 * @param onRefresh Callback to trigger refresh
 * @param modifier Modifier for the container
 * @param content The content that supports pull-to-refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        content()
    }
}

/**
 * Alternative name for better readability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    PullToRefreshContainer(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content
    )
}
