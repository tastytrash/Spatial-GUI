plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.2-neoforge"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")
}