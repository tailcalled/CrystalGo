package crystalgo.server

import javafx.embed.swing.JFXPanel
import javax.swing.SwingUtilities
import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.control.TabPane
import javafx.scene.control.Tab
import javafx.scene.layout.StackPane
import javafx.scene.Scene
import javafx.scene.layout.GridPane
import javafx.scene.control.TextField
import javafx.scene.control.Label
import javafx.geometry.Pos
import javafx.scene.control.Spinner
import javafx.scene.control.CheckBox
import javafx.scene.control.Button
import javafx.beans.binding.Bindings
import java.util.concurrent.Callable
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.scene.layout.BorderPane
import javafx.scene.control.TextArea
import javafx.stage.WindowEvent
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import java.util.Optional
import java.util.function.Consumer
import javafx.application.Platform
import javafx.scene.control.ScrollPane
import javafx.scene.control.ScrollPane.ScrollBarPolicy
import javafx.scene.image.Image
import javafx.scene.image.ImageView

object CrystalUI {
  
  val cross = new Image(classOf[CrystalUI].getResourceAsStream("images/cross.png"))
  val borderl = new Image(classOf[CrystalUI].getResourceAsStream("images/borderl.png"))
  val borderd = new Image(classOf[CrystalUI].getResourceAsStream("images/borderd.png"))
  val borderr = new Image(classOf[CrystalUI].getResourceAsStream("images/borderr.png"))
  val borderu = new Image(classOf[CrystalUI].getResourceAsStream("images/borderu.png"))
  val black = new Image(classOf[CrystalUI].getResourceAsStream("images/black.png"))
  val white = new Image(classOf[CrystalUI].getResourceAsStream("images/white.png"))
  val crossl = new Image(classOf[CrystalUI].getResourceAsStream("images/crossl.png"))
  val crossd = new Image(classOf[CrystalUI].getResourceAsStream("images/crossd.png"))
  val crossr = new Image(classOf[CrystalUI].getResourceAsStream("images/crossr.png"))
  val crossu = new Image(classOf[CrystalUI].getResourceAsStream("images/crossu.png"))
  val logo = new Image(classOf[CrystalUI].getResourceAsStream("images/logo.png"))
  val logosmall = new Image(classOf[CrystalUI].getResourceAsStream("images/logosmall.png"))
  
  def load() = {}
  
}
class CrystalUI extends Application {
  
  def start(stage: Stage) = {
    CrystalUI.load()
    val tabs = new TabPane()
    val menuTab = new Tab("Menu")
    menuTab.setClosable(false)
    var servers = Vector[CrystalServer]()
    val menu = makeMenu((server, name) => {
      servers :+= server
      val gameTab = new Tab(name)
      val game = makeGame(server)
      gameTab.setContent(game)
      tabs.getTabs.add(gameTab)
      gameTab.setOnClosed(new EventHandler[Event]() {
        def handle(ev: Event) = {
          server.close()
        }
      })
      tabs.getSelectionModel.select(gameTab)
    })
    menuTab.setContent(menu)
    tabs.getTabs.add(menuTab)
    stage.getIcons.add(CrystalUI.logosmall)
    stage.setScene(new Scene(tabs))
    stage.show()
    stage.setOnCloseRequest(new EventHandler[WindowEvent]() {
      def handle(ev: WindowEvent) = {
        if (tabs.getTabs.size() > 1) {
          val confirm = new Alert(Alert.AlertType.CONFIRMATION,
              "You have Go games open. Are you sure you want to close the server application?")
          if (confirm.showAndWait() != Optional.of(ButtonType.OK)) {
            ev.consume()
          }
          else for(s <- servers) {
            s.close()
          }
        }
      }
    })
  }

  def makeMenu(showGameTab: (CrystalServer, String) => Unit) = {
    val root = new StackPane()
    val grid = new GridPane()
    root.getChildren.add(grid)
    val logoLabel = new Label("", new ImageView(CrystalUI.logo))
    GridPane.setConstraints(logoLabel, 0, 0, 15, 1)
    grid.getChildren.addAll(logoLabel)
    val titleLabel = new Label("Title:")
    val titleBox = new TextField("Go Game")
    GridPane.setConstraints(titleLabel, 0, 1)
    GridPane.setConstraints(titleBox, 4, 1)
    grid.getChildren.addAll(titleLabel, titleBox)
    val sizeLabel = new Label("Size:")
    val sizeBox = new Spinner[Integer](11, 19, 19, 1)
    GridPane.setConstraints(sizeLabel, 0, 2)
    GridPane.setConstraints(sizeBox, 4, 2)
    grid.getChildren.addAll(sizeLabel, sizeBox)
    val komiLabel = new Label("Komi:")
    val komiBox = new Spinner[Integer](5, 12, 7, 1)
    val komiLabel2 = new Label("+ ½")
    GridPane.setConstraints(komiLabel, 0, 3)
    GridPane.setConstraints(komiBox, 4, 3)
    GridPane.setConstraints(komiLabel2, 5, 3)
    grid.getChildren.addAll(komiLabel, komiBox, komiLabel2)
    val portLabel = new Label("Port:")
    val portBox = new Spinner[Integer](0, 65535, 8448, 1)
    GridPane.setConstraints(portLabel, 0, 4)
    GridPane.setConstraints(portBox, 4, 4)
    grid.getChildren.addAll(portLabel, portBox)
    val modulesLabel = new Label("Modules:")
    val chatModuleBox = new CheckBox("Chat"); chatModuleBox.setSelected(true)
    val namesModuleBox = new CheckBox("Nicknames"); namesModuleBox.setSelected(true)
    GridPane.setConstraints(modulesLabel, 14, 1)
    GridPane.setConstraints(chatModuleBox, 14, 2)
    GridPane.setConstraints(namesModuleBox, 14, 3)
    grid.getChildren.addAll(modulesLabel, chatModuleBox,  namesModuleBox)
    val hostButton = new Button("Host")
    hostButton.setMaxWidth(Double.MaxValue)
    GridPane.setConstraints(hostButton, 14, 4)
    grid.getChildren.addAll(hostButton)
    grid.setHgap(5); grid.setVgap(5)
    grid.setAlignment(Pos.CENTER)
    root.setMinSize(32 * 19 + 400, 32 * 19 + 100)
    
    namesModuleBox.disableProperty.bind(Bindings.createBooleanBinding(new Callable[java.lang.Boolean]() {
      def call() = !chatModuleBox.isSelected()
    }, chatModuleBox.selectedProperty))
    hostButton.setOnAction(new EventHandler[ActionEvent]() {
      def handle(ev: ActionEvent) = {
        val game = new CrystalServer(
            port = portBox.getValue,
            komiMinusHalf = komiBox.getValue,
            size = sizeBox.getValue
        )
        if (chatModuleBox.isSelected()) {
          val chat = new CrystalModule.Chat
          if (namesModuleBox.isSelected()) {
            val nick = new CrystalModule.Nick
            chat.install("nick", nick)
          }
          game.install("chat", chat)
        }
        showGameTab(game, titleBox.getText)
      }
    })
    
    root
  }
  def makeGame(server: CrystalServer) = {
    val root = new BorderPane()
    val game = new StackPane()
    game.setMinSize(32 * 19 + 10, 32 * 19 + 10)
    root.setCenter(game)
    val sidebar = new BorderPane()
    root.setRight(sidebar)
    val events = new TextArea()
    events.setWrapText(true)
    events.setEditable(false)
    events.setMaxWidth(350)
    sidebar.setCenter(events)
    val details = new Label("Go Game\nDETAILS NYI")
    sidebar.setTop(details)
    val tools = new Label("Tools NYI")
    sidebar.setBottom(tools)
    
    def showBoard(b: Board) = {
      val board = makeBoard(b)
      board.setAlignment(Pos.CENTER)
      game.getChildren.setAll(board)
    }
    server.install("crystalui", new CrystalModule {
      
      def join(client: Client, id: Int) = {}
      def message(sender: Client, id: Int, message: String) = {}
      def installHooks(mirror: Mirror) = {
        mirror.sendHook ((client, id, pro) => pro match {
          case ProS2C.Snapshot(board, _, _, _) => Platform.runLater(new Runnable() {
            def run() = showBoard(board)
          })
          case _ =>
        })
        mirror.recieveHook((client, id, pro) => events.appendText(s"$id: $pro\n"))
      }
  
    })
    
    game.getChildren.setAll(new Label("", new ImageView(CrystalUI.logo)))
    
    root
  }
  
  def makeBoard(board: Board) = {
    val grid = new GridPane()
    
    for (x <- 0 until board.width) {
      for (y <- 0 until board.height) {
        val cell = new StackPane()
        var parts = Vector[Image]()
        parts :+= CrystalUI.cross
        if (x+1 < board.width) parts :+= CrystalUI.crossr
        else parts :+= CrystalUI.borderr
        if (y+1 < board.height) parts :+= CrystalUI.crossd
        else parts :+= CrystalUI.borderd
        if (x > 0) parts :+= CrystalUI.crossl
        else parts :+= CrystalUI.borderl
        if (y > 0) parts :+= CrystalUI.crossu
        else parts :+= CrystalUI.borderu
        board.stones.get((x, y)) match {
          case None =>
          case Some(Black) => parts :+= CrystalUI.black
          case Some(White) => parts :+= CrystalUI.white
        }
        cell.getChildren.setAll(parts.map(i => new Label("", new ImageView(i))): _*)
        GridPane.setConstraints(cell, x, y)
        grid.getChildren.add(cell)
      }
    }
    
    grid
  }
  
}