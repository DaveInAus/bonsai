package cafe.adriel.bonsai.filesystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.bonsai.core.BonsaiStyle
import cafe.adriel.bonsai.core.node.BranchNode
import cafe.adriel.bonsai.core.node.Node
import cafe.adriel.bonsai.core.node.SimpleBranchNode
import cafe.adriel.bonsai.core.node.SimpleLeafNode
import okio.FileSystem
import okio.Path

internal data class FileSystemNodeScope(
    val fileSystem: FileSystem
)

public fun FileSystemBonsaiStyle(): BonsaiStyle<Path> =
    BonsaiStyle(
        nodeNameStartPadding = 4.dp,
        nodeCollapsedIcon = { node ->
            rememberVectorPainter(
                if (node is BranchNode) Icons.Outlined.Folder
                else Icons.Outlined.InsertDriveFile
            )
        },
        nodeExpandedIcon = {
            rememberVectorPainter(Icons.Outlined.FolderOpen)
        }
    )

public fun fileSystemNodes(
    rootPath: Path,
    fileSystem: FileSystem,
    selfInclude: Boolean = false
): List<Node<Path>> =
    with(
        FileSystemNodeScope(
            fileSystem = fileSystem
        )
    ) {
        fileSystemNodes(
            rootPath = rootPath,
            parent = null,
            selfInclude = selfInclude,
        )
    }

private fun FileSystemNodeScope.fileSystemNodes(
    rootPath: Path,
    parent: Node<Path>?,
    selfInclude: Boolean = false
): List<Node<Path>> =
    if (selfInclude) {
        listOf(fileSystemNode(rootPath, parent))
    } else {
        fileSystem
            .listOrNull(rootPath)
            ?.map { path -> fileSystemNode(path, parent) }
            .orEmpty()
    }

private fun FileSystemNodeScope.fileSystemNode(
    path: Path,
    parent: Node<Path>?
) =
    if (fileSystem.metadata(path).isDirectory) {
        SimpleBranchNode(
            content = path,
            name = path.name,
            parent = parent,
            children = { node -> fileSystemNodes(path, node) }
        )
    } else {
        SimpleLeafNode(
            content = path,
            name = path.name,
            parent = parent
        )
    }
