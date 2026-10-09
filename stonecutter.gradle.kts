plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.1.2-fabric"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge", "forge")

    replacements {
        string(current.parsed < "26.2") {
            replace("gameRenderer.mainCamera()", "gameRenderer.getMainCamera()")
            replace("= client.gui.screen()", "= client.screen")
        }

        string(current.parsed < "1.21.1") {
            replace("modelView.pushMatrix()", "modelView.pushPose()")
            replace("modelView.popMatrix()", "modelView.popPose()")
        }

        string(current.parsed < "1.21.11") {
            replace("camera.yRot()", "camera.getYRot()")
            replace("camera.xRot()", "camera.getXRot()")
            replace(".grabOrReleaseMouse(mc.getWindow(), ", ".grabOrReleaseMouse(mc.getWindow().getWindow(), ")
            replace("invertMouseY()", "invertYMouse()")
        }

        string(current.parsed < "1.21.11") {
            replace(
                "me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen",
                "me.shedaniel.autoconfig.AutoConfig.getConfigScreen"
            )
        }
    }
}