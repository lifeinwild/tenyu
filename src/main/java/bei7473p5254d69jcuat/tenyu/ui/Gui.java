package bei7473p5254d69jcuat.tenyu.ui;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import com.sun.javafx.scene.control.behavior.*;
import com.sun.javafx.scene.control.skin.*;

import bei7473p5254d69jcuat.tenyu.ui.common.*;
import bei7473p5254d69jcuat.tenyu.ui.common.TableRow;
import glb.*;
import glb.Conf.*;
import glb.Glb.*;
import javafx.application.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.control.TabPane.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import javafx.util.*;
import tenyuproject.*;

/**
 * 本アプリのGUI全体を構築、保持する。
 *
 * GUI全体の設計について。まずJavaFXベースで、FXMLは使用していない。
 * CSSデザインのため、可能な限りGUI部品にIDを振っている。
 * 根本にTabPaneがあり、TabPaneは複数のタブを持ち、
 * 他のGUI部品はいずれかのタブに含まれる。
 *
 * 操作方法。
 * ctrl+方向キーでタブ移動やGUI部品の間の移動ができる。
 * スペースでGUI部品を選択してGUI部品に応じたアクションを起こせる。
 *
 * テキストエリアはctrl+方向キーを独自に扱っていて、
 * このGUIシステム全体のショートカットキーと被ってしまう。
 * テキストエリアの機能を無視するようにした。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Gui implements GlbMemberDynamicState {
	private Stage primary = null;
	private Group root = null;
	private Scene scene = null;
	/**
	 * GUI全体はタブベースで作られる。
	 * 他のあらゆるGUI部品はいずれかのタブの内容として設置される。
	 */
	private TabPane basePane = null;
	/**
	 * 最初からあり、削除不可能なタブ。
	 */
	private Tab outlineTab = null;
	/**
	 * 機能一覧を表示する。
	 * TODO:TreeViewの設計が少し違和感があって、カテゴリと末端の要素のクラスが異なる場合、
	 * カテゴリと末端の要素のクラスが同じでなければならないから、
	 * カテゴリについても末端の要素と同じクラスとして作ってnullを返す等実際には機能しない
	 * ようにして、nullが返るかどうかで動作を分けるようにする事になる。
	 * 要素のクラスをStringにしてswitch構文で、というのも考えられるが、
	 * 要素のクラスをGuiBuilderにしてカテゴリーを意味する要素ではnullを返す方式にした。
	 */
	private TreeView<GuiBuilder> outlinePane = null;

	private KeyCombination ctrlLeft = KeyCombination.valueOf("Shortcut+Left");
	private KeyCombination ctrlRight = KeyCombination.valueOf("Shortcut+Right");
	private KeyCombination ctrlUp = KeyCombination.valueOf("Shortcut+Up");
	private KeyCombination ctrlDown = KeyCombination.valueOf("Shortcut+Down");
	/**
	 * Ctrl+SpaceはSpace単品入力に対応しているので無くても良いが、
	 * Ctrl+方向キーで移動しているとctrlを押したままになりがちなので追加する。
	 */
	private KeyCombination ctrlSpace = KeyCombination.valueOf("Shortcut+Space");

	/**
	 * ctrl+←で前のタブに移動
	 */
	private Runnable ctrlLeftHandler = () -> {
		SingleSelectionModel<Tab> select = basePane.getSelectionModel();
		if (select.getSelectedIndex() == 0) {
			select.selectLast();
		} else {
			select.selectPrevious();
		}
		select.getSelectedItem().getContent().requestFocus();
	};

	/**
	 * ctrl+→で次のタブに移動
	 */
	private Runnable ctrlRightHandler = () -> {
		//-2になるのは、恐らく表面的に見えているタブ以外のものが追加されているから
		int lastIndex = basePane.getChildrenUnmodifiable().size() - 2;
		SingleSelectionModel<Tab> select = basePane.getSelectionModel();
		if (select.getSelectedIndex() == lastIndex) {
			select.selectFirst();
		} else {
			select.selectNext();
		}
		//タブ移動に合わせて移動先のタブの内容にフォーカス
		//なおselectedItemPropertyやvisibleProperty等ではうまくいかなかった
		//requestFocus()がある種の条件下でしか動作しないせいと思われる。
		//requestFocus()直後にブレークしてisFocused()を調べてもfalseだった。
		select.getSelectedItem().getContent().requestFocus();
	};

	/**
	 * ctrl+↑でフォーカスを上に移動
	 */
	private Runnable ctrlUpHandler = () -> {
		Tab selectedTab = basePane.getSelectionModel().getSelectedItem();
		Node selectedTabContent = selectedTab.getContent();
		Pane p = null;
		if (selectedTabContent instanceof Pane) {
			p = (Pane) selectedTabContent;
		} else if (selectedTabContent instanceof ScrollPane) {
			ScrollPane sp = (ScrollPane) selectedTabContent;
			if (sp.getContent() instanceof Pane) {
				p = (Pane) sp.getContent();
			}
		}
		if (p == null)
			return;
		if (!tabEventToFocused(p, true)) {
			p.getChildrenUnmodifiable()
					.get(p.getChildrenUnmodifiable().size() - 1).requestFocus();
		}
	};

	/**
	 * ctrl+↓でフォーカスを下に移動
	 */
	private Runnable ctrlDownHandler = () -> {
		Tab selectedTab = basePane.getSelectionModel().getSelectedItem();
		Node selectedTabContent = selectedTab.getContent();
		Pane p = null;
		if (selectedTabContent instanceof Pane) {
			p = (Pane) selectedTabContent;
		} else if (selectedTabContent instanceof ScrollPane) {
			ScrollPane sp = (ScrollPane) selectedTabContent;
			if (sp.getContent() instanceof Pane) {
				p = (Pane) sp.getContent();
			}
		}
		if (p == null)
			return;

		if (!tabEventToFocused(p, false)) {
			p.getChildrenUnmodifiable().get(0).requestFocus();
		}
	};

	/**
	 * ctrl+Spaceで単にSpaceのイベントを発生させる
	 */
	private Runnable ctrlSpaceHandler = () -> {
		Tab selectedTab = basePane.getSelectionModel().getSelectedItem();
		Node selectedTabContent = selectedTab.getContent();
		Pane p = null;
		if (selectedTabContent instanceof Pane) {
			p = (Pane) selectedTabContent;
		} else if (selectedTabContent instanceof ScrollPane) {
			ScrollPane sp = (ScrollPane) selectedTabContent;
			if (sp.getContent() instanceof Pane) {
				p = (Pane) sp.getContent();
			}
		}
		if (p == null)
			return;

		if (!spaceEventToFocused(p)) {
		}

	};

	public void setTitle(String prefix, String suffix) {
		//各要素の間に入る空白
		String indent = "    ";

		//接頭辞があればつける
		StringBuilder sb = new StringBuilder();
		if (prefix != null && prefix.length() > 0)
			sb.append(prefix).append(indent);

		//実行モードに応じて文字列を表示する
		RunLevel runLevel = Glb.getConf().getRunlevel();
		String mode = runLevel.equals(RunLevel.RELEASE) ? null
				: runLevel.toString();
		if (mode != null && mode.length() > 0)
			sb.append(mode).append(indent);

		//アプリ名を表示する
		sb.append(Glb.getConst().getAppName());

		//同調状況を表示する
		sb.append("sync");
		if (Glb.getMiddle().getObjeCatchUp().imVeteran()) {
			sb.append("〇");
		} else {
			sb.append("×");
		}

		//接尾辞があればつける
		if (suffix != null && suffix.length() > 0)
			sb.append(indent).append(suffix);

		//ウィンドウタイトルに設定
		primary.setTitle(sb.toString());
	}

	/**
	 * GUIを構築する。このメソッドを呼ぶだけで本アプリのGUI全体が構築される。
	 * @param primaryStage
	 */
	public void build(Stage primaryStage) {
		try {
			this.primary = primaryStage;
			setTitle(null, null);
			Image ico = Glb.getFile().getIcon();
			primaryStage.getIcons().add(ico);

			//Base
			root = new Group();
			root.setId("root");
			scene = new Scene(root, 800, 600, Color.WHITE);
			KeyCombination ctrlW = KeyCombination.valueOf("Shortcut+W");
			scene.getAccelerators().put(ctrlLeft, ctrlLeftHandler);
			scene.getAccelerators().put(ctrlRight, ctrlRightHandler);
			//ctrl+wで現在表示されているタブを削除する
			scene.getAccelerators().put(ctrlW, () -> {
				int index = basePane.getSelectionModel().getSelectedIndex();
				if (index != 0)
					basePane.getTabs().remove(index);
			});
			scene.getAccelerators().put(ctrlUp, ctrlUpHandler);
			scene.getAccelerators().put(ctrlDown, ctrlDownHandler);
			scene.setOnKeyPressed((ev) -> {
				if (ev.getCode() == KeyCode.F5)
					loadCss();
			});
			loadCss();

			//TabPane
			basePane = new TabPane();
			basePane.setId("tabPane");
			basePane.prefHeightProperty().bind(scene.heightProperty());
			basePane.prefWidthProperty().bind(scene.widthProperty());
			root.getChildren().add(basePane);
			basePane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
			basePane.setOnKeyPressed((ev) -> {
				Glb.debug("tab pressed : "
						+ KeyCode.valueOf(ev.getCode().name()));
				if (ev.getCode() == KeyCode.DOWN
						|| ev.getCode() == KeyCode.UP) {
					//現在表示されているタブの内容
					Node content = basePane.getSelectionModel()
							.getSelectedItem().getContent();
					//内容の中で１つもフォーカスされているものが無いか
					boolean noFocuse = false;
					if (content instanceof Pane) {
						Pane p = (Pane) content;
						noFocuse = !recursiveSearch(p, (n) -> {
							return n.isFocused();
						});
					} else if (content instanceof ScrollPane) {
						ScrollPane sp = (ScrollPane) content;
						Pane p = (Pane) sp.getContent();
						noFocuse = !recursiveSearch(p, (n) -> {
							return n.isFocused();
						});
						if (noFocuse) {
							p.requestFocus();
						}
						return;
					} else {
						noFocuse = !content.isFocused();
					}
					//１つもフォーカスされていない場合、フォーカスする
					//この処理は上下キーによるタブから内容へのフォーカス移動を実現する
					//常にctrlを押して移動している場合常に何らかの内容がフォーカスされていて必要無いが、
					//もしctrlを離して操作した場合タブにフォーカスが移動する場合がある。
					if (noFocuse) {
						content.requestFocus();
					}
				}
			});

			//outlineTab
			outlineTab = new Tab();
			outlineTab.setClosable(false);
			outlineTab.setId("outlineTab");
			outlineTab.setText(Lang.OUTLINE.toString());
			basePane.getTabs().add(outlineTab);

			outlinePane = new TreeViewBuilder().build();
			outlineTab.setContent(outlinePane);

			primaryStage.setScene(scene);
			primaryStage.show();

			//初期フォーカス
			outlinePane.requestFocus();

			//開発モードではダイアログを出さない
			//TODO:一時的に反転している
			if (!Glb.getConf().getRunlevel().equals(RunLevel.DEV)) {
				/**
				 * 開発モードの場合、仮のパスワードを自動的に使う。
				 * 基本的にこのようなコードはテストコード側に置くべきだが、
				 * Guiで開発モード時にパスワード入力ダイアログを出さない方が楽なのでここに。
				 * テストコードのimportが発生するのは気付かない可能性があるのでまずい。
				 * このコードはリリース直前に取り除く。
				 */
				//開発モード時に使用される秘密鍵暗号化のパスワード
				String devPassword = TenyuTest.devPassword;
				Glb.getConf().getKeys().init2(devPassword);
			} else {
				boolean isKeyGenerated = Glb.getConf().getKeys()
						.isKeyGenerated();
				//秘密鍵パスワード入力ダイアログ
				Node dialog = null;
				if (isKeyGenerated) {
					dialog = new PasswordDialogDecryptBuilder().build();
				} else {
					dialog = new PasswordDialogEncryptBuilder().build();
				}
				//writeTypeAndId(dialog);
			}

			//タイトルバーにショートカットキーを表示
			String shortcuts = Lang.SHORTCUT_KEY + "    "
					+ Lang.SHORTCUT_KEY_CONTENT;
			setTitle("", shortcuts);

			//writeTypeAndId(root);
			//writeTypeAndId(basePane);
			//writeTypeAndId(outlinePane);
		} catch (

		Exception e) {
			Glb.getLogger().error("", e);
			System.exit(1);
		}

	}

	public void start() {
		started = true;
	}

	public void stop() {
		started = false;
	}

	public void createTab(Node p, String tabName) {
		try {
			if (p == null)
				return;
			//Gui.writeTypeAndId(p);
			TabPane tabPane = Glb.getGui().getBasePane();
			//スクロール可能に
			ScrollPane sp = new ScrollPane();
			sp.setContent(p);
			sp.setFitToWidth(true);
			//sp.setCache(true);
			/*
			sp.setOnMouseEntered((ev) -> {
				//このURLのアイデアに加え、キーボード操作の時はフォーカス移動で勝手にスクロールされるから、
				//MouseEnteredイベントの時にスクロール距離を変えるようにした。
				//https://stackoverflow.com/questions/28904312/how-to-access-scrollbar-in-a-scrollpane-javafx
				Set<Node> nodes = sp.lookupAll(".scroll-bar");
				for (Node n : nodes) {
					if (n instanceof ScrollBar) {
						ScrollBar sb = (ScrollBar) n;
						sb.setUnitIncrement(5000);
						sb.setBlockIncrement(5000);
					}
				}
			});*/

			//結局うまくいかなかった
			//https://stackoverflow.com/questions/32739269/how-do-i-change-the-amount-by-which-scrollpane-scrolls
			/*
			p.setOnScroll((ev) -> {
				Glb.debug("scrolling Vvalue=" + sp.getVvalue());
			});
			*/

			/*
			sp.setOnScrollStarted(new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent scrollEvent) {
					Glb.debug("setOnScrollStarted");
				}
			});

			sp.setOnScrollFinished(new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent scrollEvent) {
					Glb.debug("setOnScrollFinished");
				}
			});

			//スクロールが一番上または一番下に到達した場合のみ呼ばれる。なんだこれは？
			sp.setOnScroll(new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent scrollEvent) {
					Glb.debug("setOnScroll");
				}
			});
			*/

			if (p != null) {
				//タブ作成
				Tab tab = new Tab();
				tab.setText(tabName);

				//タブに内容設置
				tab.setContent(sp);

				//タブ一覧にタブ追加
				tabPane.getTabs().add(tab);

				//タブ移動
				tabPane.getSelectionModel().select(tab);

				Gui.registerHandler(sp);
			}
		} catch (Exception e) {
			Glb.debug(e);
		}
	}

	public static void writeTypeAndId(Node start) {
		try {
			if (start == null)
				return;
			StringBuilder sb = new StringBuilder();
			if (start.getId() != null)
				sb.append("id=" + start.getId() + System.lineSeparator());
			if (start.getTypeSelector() != null)
				sb.append("type=" + start.getTypeSelector()
						+ System.lineSeparator());
			sb.append(System.lineSeparator());

			if (start instanceof Pane) {
				Pane p = (Pane) start;
				recursiveSearch(p, (n) -> {
					if (start.getId() != null)
						sb.append("id=" + n.getId() + System.lineSeparator());
					if (start.getTypeSelector() != null)
						sb.append("type=" + n.getTypeSelector()
								+ System.lineSeparator());
					sb.append(System.lineSeparator());
					return false;
				});
			}
			String path = Glb.getFile().getTypeSelectorIdLogPath();

			File file = Glb.getFile().get(path);
			if (!file.exists())
				Glb.getFile().create(Paths.get(path), null, false);
			if (!file.exists() || !file.isFile() || !file.canWrite()) {
				return;
			}
			FileWriter writer = new FileWriter(file, true);
			writer.write(sb.toString());
			writer.close();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			//オプションな処理なので失敗しても実動作に問題無し
		}
	}

	public boolean removeTypeSelectorIdLog() {
		try {
			String path = Glb.getFile().getTypeSelectorIdLogPath();
			File file = Glb.getFile().get(path);
			if (!file.exists())
				return false;
			return file.delete();
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return false;
		}
	}

	private static boolean tabEventToFocused(Pane p, boolean shift) {
		return keyEventToFocused(p, new KeyEvent(KeyEvent.KEY_PRESSED, "", "",
				KeyCode.TAB, shift, false, false, false));
	}

	private static boolean spaceEventToFocused(Pane p) {
		//TODO:KeyEvent.SPACEでもいけるはずだが、動作しなかった。これで目的の動作はしている
		return keyEventToFocused(p, new ActionEvent());
	}

	/**
	 * フォーカスされているGUI部品を見つけてキーが押されたイベントを伝える
	 * @param p			探索開始地点となる枠
	 * @param ev		伝達されるキーイベント
	 * @return			フォーカスされているGUI部品を見つけてTABキーイベントを伝えたか
	 */
	private static boolean keyEventToFocused(Pane p, Event ev) {
		return recursiveSearch(p, (n) -> {
			if (n.isFocused()) {
				Glb.debug(
						"Event fire " + " class=" + n.getClass().getSimpleName()
								+ " id=" + n.getId() + " ev=" + ev);
				n.fireEvent(ev);
				return true;
			}
			return false;
		});
	}

	/**
	 * GUI部品を探索してハンドラ登録
	 */
	public static void registerHandler(Node start) {
		//アプリ全体で通用するキーボードショートカットの登録処理
		Function<Node, Boolean> f = (n) -> {
			//ハンドラを登録済みなら登録しない
			//独自のハンドラを登録した箇所はGlobal系ハンドラを登録すべき
			if (n.getOnKeyPressed() != null) {
				System.out.println(n.getClass().getSimpleName());
				return true;
			}
			n.setOnKeyPressed(new GlobalKeyPressedEventHandler(n));
			return false;//全て探索する事を意味する
		};
		//Paneなら再帰的探索をして全てに登録
		if (start instanceof Pane) {
			Pane p = (Pane) start;
			recursiveSearch(p, f);
		} else if (start instanceof ScrollPane) {
			registerHandler(((ScrollPane) start).getContent());
		} else {
			//Nodeなら単品だけ登録
			f.apply(start);
		}
	}

	public static class GlobalKeyPressedEventHandler
			implements EventHandler<KeyEvent> {
		private Node n;

		public GlobalKeyPressedEventHandler(Node n) {
			this.n = n;
		}

		@Override
		public void handle(KeyEvent ev) {
			Gui gui = Glb.getGui();
			if (gui.getCtrlLeft().match(ev)) {
				gui.getCtrlLeftHandler().run();
			} else if (gui.getCtrlRight().match(ev)) {
				gui.getCtrlRightHandler().run();
			} else if (n instanceof TableView) {
				//一部のGUI部品はsceneに登録されたctrlUp,ctrlDownのハンドラが
				//効かないので、そのようなものを見つけるたびここにinstanceofを追加する
				if (gui.getCtrlUp().match(ev)) {
					gui.getCtrlUpHandler().run();
				} else if (gui.getCtrlDown().match(ev)) {
					gui.getCtrlDownHandler().run();
				}
			} else if (n instanceof TextArea) {
				//TextAreaはctrl+up,ctrl+down,tabが別の意味を持つ
				try {
					TextArea textArea = (TextArea) n;
					TextAreaSkin skin = (TextAreaSkin) textArea.getSkin();
					if (skin.getBehavior() instanceof TextAreaBehavior) {
						TextAreaBehavior behavior = (TextAreaBehavior) skin
								.getBehavior();

						if (gui.getCtrlUp().match(ev)) {
							behavior.callAction("TraversePrevious");
						} else if (gui.getCtrlDown().match(ev)) {
							behavior.callAction("TraverseNext");
						}
						//TODO なぜこのコードを書いたか不明。あるとテキストエリアでカーソル位置を変更できなくなる。バックスペースも効かない。ev.connsume();
						//やはり単独の開発でもgitで細かく修正理由を記録すべきか
					}
				} catch (Exception e) {
					//このコードはjre依存がかなり出そう
					Glb.debug(e);
				}
			} else if (n instanceof Control) {
				//Control系はctrl+spaceが効かないのでハンドラ登録
				if (gui.getCtrlSpace().match(ev)) {
					gui.getCtrlSpaceHandler().run();
				}
			}
		}
	};

	/**
	 * GUI部品を網羅的に探索してfを適用する
	 * fがtrueを返した時点で探索は終わる
	 * fがtrueを返さなければ全要素にfが適用されるので
	 * 処理が成功してもfalseを返す事で複数の要素に適用できる
	 * 標準のPane#lookup系メソッドもあるが、処理を渡せない。
	 * @param p		探索開始地点となる枠
	 * @param f		適用される処理
	 * @return		fが1回でもtrueを返した
	 */
	private static boolean recursiveSearch(Pane p, Function<Node, Boolean> f) {
		List<Node> nodes = new ArrayList<>(p.getChildrenUnmodifiable());
		for (Node n : nodes) {
			if (f.apply(n)) {
				return true;
			}
			if (n instanceof Pane) {
				if (recursiveSearch((Pane) n, f))
					return true;
			}
		}
		return false;
	}

	private boolean started = false;

	public void alert(AlertType type, String title, String content) {
		if (!started) {
			Glb.getLogger().warn("Not started", new IllegalStateException());
			return;
		}
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Alert a = new Alert(type);
					a.setTitle(title);
					Stage stage = (Stage) a.getDialogPane().getScene()
							.getWindow();
					stage.getIcons().add(Glb.getFile().getIcon());
					a.setContentText(content);
					a.show();
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
			}
		};

		runByFXThread(r);
	}

	/**
	 * FXアプリケーションスレッドで実行
	 *
	 * 描画系処理はFXスレッドで実行しないと不安定になる。
	 * 一方で通信処理などはFXスレッドで実行すると描画が停止するので、
	 * FXアプリはその点を留意しながら書く必要がある。
	 *
	 * @param r
	 */
	public void runByFXThread(Runnable r) {
		Platform.runLater(r);
	}

	private boolean loadCss() {
		try {
			String css = Glb.getUtil().getLoader()
					.getResource(Glb.getFile().getCss()).toExternalForm();
			scene.getStylesheets().remove(css);
			return scene.getStylesheets().add(css);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 各GUI部品から使うユーティリティメソッド
	 * ポーリングスレッドの作成と停止処理の登録
	 *
	 * @param grid			これが消される時ポーリングが停止する
	 * @param taskCreator	定期的に実行したい処理。GUIの情報更新など
	 */
	public void polling(GridPane grid, Supplier<Task<Void>> taskCreator) {
		Glb.debug("polling");

		//ポーリングで一覧を定期更新
		ScheduledService<Void> polling = new ScheduledService<Void>() {
			@Override
			protected Task<Void> createTask() {
				return taskCreator.get();
			}
		};
		polling.setDelay(Duration.seconds(0));
		polling.setPeriod(Duration.seconds(4));
		polling.start();

		//ポーリングスレッドを停止
		grid.sceneProperty().addListener((arg0, oldScene, newScene) -> {
			if (newScene == null && polling != null && polling.isRunning()) {
				polling.cancel();
				Glb.debug("polling interrupted");
			}
		});
	}

	public static final Task<Void> nullTask = new Task<Void>() {
		@Override
		protected Void call() throws Exception {
			return null;
		}
	};

	/**
	 * TableViewを定期更新する
	 * 各GUI部品から使うユーティリティメソッド
	 * ポーリングスレッドが定期的に行うTaskを作成する
	 * Srcはequalsを実装していること。
	 *
	 * @param modelSrc		モデル側のリスト。実行のたびに新しいリストを作成するためラムダで渡す
	 * @param createRow		渡されたモデルデータをGUI表示用オブジェクトにする
	 * @param table			モデルのリストを表示しているTableView
	 * @return				TableViewを定期更新するTask
	 */
	public <Src,
			Row extends TableRow<Src>> Task<Void> createPollingTaskTableView(
					Supplier<List<Src>> modelSrc, Function<Src, Row> createRow,
					TableView<Row> table) {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				if (isCancelled())
					return null;
				updateTable(modelSrc.get(), createRow, table);
				return null;
			}
		};
	}

	public <Src, Row extends TableRow<Src>> void updateTable(
			Collection<Src> models, Function<Src, Row> createRow,
			TableView<Row> table) {
		//以下のコードは、オブジェクトの中身はバインディングによって
		//最新値が表示されるので、オブジェクトのリストについて更新する処理を加えて、
		//オブジェクトリストの最新値を表示するもの。

		//GUIへの追加候補	モデルから取得した表示されるべきデータ
		List<Src> addCandidates = new ArrayList<>(models);
		for (int i = 0; i < table.getItems().size(); i++) {
			Row item = table.getItems().get(i);
			boolean exist = addCandidates.contains(item.getSrc());
			if (exist) {
				//GUIにある　追加候補にある　追加候補から削除
				addCandidates.remove(item.getSrc());
			} else {
				//GUIにある　追加候補に無い　GUIから削除
				table.getItems().remove(i);
			}
			item.update();
		}

		//GUIに無い　モデルリストにある　GUIに追加
		for (Src s : addCandidates) {
			table.getItems().add(createRow.apply(s));
		}
	}

	private TextArea statusTextView;

	public TextArea getStatusTextView() {
		return statusTextView;
	}

	public void setStatusTextView(TextArea statusTextView) {
		this.statusTextView = statusTextView;
	}

	public Group getRoot() {
		return root;
	}

	public Scene getScene() {
		return scene;
	}

	public TabPane getBasePane() {
		return basePane;
	}

	public Tab getOutlineTab() {
		return outlineTab;
	}

	public TreeView<GuiBuilder> getOutlinePane() {
		return outlinePane;
	}

	public KeyCombination getCtrlLeft() {
		return ctrlLeft;
	}

	public KeyCombination getCtrlRight() {
		return ctrlRight;
	}

	public KeyCombination getCtrlUp() {
		return ctrlUp;
	}

	public KeyCombination getCtrlDown() {
		return ctrlDown;
	}

	public KeyCombination getCtrlSpace() {
		return ctrlSpace;
	}

	public Runnable getCtrlLeftHandler() {
		return ctrlLeftHandler;
	}

	public Runnable getCtrlRightHandler() {
		return ctrlRightHandler;
	}

	public Runnable getCtrlUpHandler() {
		return ctrlUpHandler;
	}

	public Runnable getCtrlDownHandler() {
		return ctrlDownHandler;
	}

	public Runnable getCtrlSpaceHandler() {
		return ctrlSpaceHandler;
	}

	public Stage getPrimary() {
		return primary;
	}

	/**
	 * GUI操作系イベントのハンドラーを非同期実行する
	 * @param handler
	 * @param ev
	 */
	/*
	private <E extends Event> void executeAsync(EventHandler<E> handler, E ev) {
		Glb.getExecutor().execute(() -> handler.handle(ev));
	}
	*/

	/**
	 * 同期処理。1スレッドで処理されるのでボタンの連打等による並列処理の発生を防止できる
	 * @param r
	 * @return
	 * @throws Exception
	 */
	public <R> R executeSync(Callable<R> r) throws Exception {
		return Glb.getExecutor().submit(r).get();
	}

	/**
	 * 返値が無いタイプ
	 * @param r
	 * @throws Exception
	 */
	public void executeSync(Runnable r) throws Exception {
		Glb.getExecutor().submit(r).get();
	}

	public void executeAsync(Runnable r) throws Exception {
		Glb.getExecutor().execute(r);
	}

	/**
	 * イベントハンドラの実行を非同期かつ共通の1スレッドによる実行にする。
	 * こうすることで送信ボタンの連打で並列処理になってしまう問題が生じなくなる。
	 *
	 * @param handler
	 * @return
	 */
	/*
	public EventHandler<ActionEvent> getCommonHandler(
			EventHandler<ActionEvent> handler) {
		return (ev) -> executeAsync(handler, ev);
	}
	*/
	/*
		public EventHandler<KeyEvent> getCommonHandlerKeyEvent(
				EventHandler<KeyEvent> handler) {
			return new EventHandler<KeyEvent>() {
				public void handle(KeyEvent ev) {
					executeAsync(handler, ev);
				};
			};
		}
	*/

}
