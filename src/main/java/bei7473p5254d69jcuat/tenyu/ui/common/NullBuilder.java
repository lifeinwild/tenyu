package bei7473p5254d69jcuat.tenyu.ui.common;

import glb.*;
import javafx.scene.layout.*;

/**
 * TreeViewの設計に対応するためのnullを返すだけのBuilder
 * @author exceptiontenyu@gmail.com
 *
 */
public class NullBuilder extends GuiBuilder {
	private final String name;

	public NullBuilder(String name) {
		this.name = name;
	}

	public NullBuilder(Lang name) {
		this(name.toString());
	}

	@Override
	public Pane build() {
		return null;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String id() {
		return name;//使われる事が無い
	}
}