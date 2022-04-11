package cafe.adriel.bonsai.core.node

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import cafe.adriel.bonsai.core.BonsaiScope
import cafe.adriel.bonsai.core.util.Saveable

public interface Node<T> : Saveable {

    public val content: T

    public val level: Int

    public val parent: Node<T>?

    public var isSelected: MutableState<Boolean>

    @Composable
    public fun NodeIcon()

    @Composable
    public fun NodeName()
}

public interface BranchNode<T> : Node<T> {

    public var isExpanded: MutableState<Boolean>

    public val children: MutableList<Node<T>>
}

public interface LeafNode<T> : Node<T>

@Composable
internal fun <T> BonsaiScope<T>.Node(
    node: Node<T>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        ToggleIcon(node)
        NodeContent(node)
    }

    if (node is BranchNode) {
        AnimatedVisibility(
            visible = node.isExpanded.value,
            enter = style.expandTransition,
            exit = style.collapseTransition
        ) {
            ExpandedNode(
                nodes = node.children,
                level = node.level
            )
        }
    }
}

@Composable
private fun <T> BonsaiScope<T>.ToggleIcon(
    node: Node<T>
) {
    if (style.toggleIcon == null) return

    if (node is BranchNode) {
        val rotationDegrees by animateFloatAsState(
            if (node.isExpanded.value && style.enableToggleIconRotation) 90f else 0f
        )

        Image(
            painter = style.toggleIcon,
            contentDescription = "Toggle",
            modifier = Modifier
                .clip(style.toggleShape)
                .then(clickableNode(node, forceSingleClick = true))
                .size(style.nodeIconSize)
                .requiredSize(style.toggleIconSize)
                .rotate(rotationDegrees)
        )
    } else {
        Spacer(Modifier.size(style.nodeIconSize))
    }
}

@Composable
private fun <T> BonsaiScope<T>.NodeContent(
    node: Node<T>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .run {
                if (node.isSelected.value.not()) clip(style.nodeShape)
                else background(style.nodeSelectedBackgroundColor, style.nodeShape)
            }
            .then(clickableNode(node))
            .padding(style.nodePadding)
            .requiredHeight(style.nodeIconSize)
    ) {
        node.NodeIcon()
        node.NodeName()
    }
}

@Composable
private fun <T> BonsaiScope<T>.ExpandedNode(
    nodes: List<Node<T>>,
    level: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = level * style.nodeIconSize)
    ) {
        nodes.forEach { node -> Node(node) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> BonsaiScope<T>.clickableNode(
    node: Node<T>,
    forceSingleClick: Boolean = false
): Modifier =
    if (forceSingleClick || onLongClick == null && onDoubleClick == null) {
        Modifier.clickable(
            role = Role.Button,
            onClick = { onClick?.invoke(node) }
        )
    } else {
        Modifier.combinedClickable(
            role = Role.Button,
            onClick = { onClick?.invoke(node) },
            onDoubleClick = { onDoubleClick?.invoke(node) },
            onLongClick = { onLongClick?.invoke(node) }
        )
    }
