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
            replace("(client.gui.screen() !=", "(client.screen !=")
            replace("client.gui.screen().resize", "client.screen.resize")
            replace("client.setScreenAndShow", "client.setScreen")
            replace("mc.setScreenAndShow", "mc.setScreen")
        }

        string(current.parsed < "26.1.2") {
            replace("g.outline", "g.renderOutline")
            replace("g.horizontalLine", "g.hLine")
            replace("g.verticalLine", "g.vLine")
        }

        string(current.parsed < "1.21.1") {
            replace("modelView.pushMatrix()", "modelView.pushPose()")
            replace("modelView.popMatrix()", "modelView.popPose()")
        }

        string(current.parsed < "1.21.11") {
            replace(".grabOrReleaseMouse(mc.getWindow(), ", ".grabOrReleaseMouse(mc.getWindow().getWindow(), ")
            replace("invertMouseY()", "invertYMouse()")
        }

        string(current.parsed < "1.21.11") {
            replace(
                "me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen",
                "me.shedaniel.autoconfig.AutoConfig.getConfigScreen"
            )
        }

        regex(current.parsed <= "1.21.11") {
            replace(
                "\\bGuiGraphicsExtractor\\b", "GuiGraphics",
                "\\bGuiGraphics\\b", "GuiGraphicsExtractor"
            )
        }
    }
}