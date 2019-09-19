package bei7473p5254d69jcuat.tenyu.release1.ui.creator.game;

import bei7473p5254d69jcuat.tenyu.release1.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.AbstractGame.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.game.AbstractGameGui.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

public abstract class AbstractGameGui<T1 extends AbstractGameDBI,
		T2 extends T1,
		T3 extends AbstractGame,
		S extends AbstractGameStore<T1, T2>,
		G extends AbstractGameGui,
		TI extends AbstractGameTableItem<T1, T2>>
		extends NaturalityGui<T1, T2, T3, S, G, TI> {
	public AbstractGameGui(String name, String id) {
		super(name, id);
	}

	private TableView<GameClientFileTableItem> clientFilesTable;

	@Override
	public void set(T3 n) {
		super.set(n);
		if (clientFilesTable != null && n.getClientFiles() != null) {
			for (TenyuFile e : n.getClientFiles()) {
				clientFilesTable.getItems()
						.add(new GameClientFileTableItem(e, null));
			}
		}
	}

	public T3 setupModelCreate() {
		T3 r = super.setupModelCreate();
		setModelByGui(r);
		return r;
	}

	private void setModelByGui(T3 n) {
		if (clientFilesTable != null) {
			for (GameClientFileTableItem e : clientFilesTable.getItems()) {
				n.addGameClientFile(e.getSrc());
			}
		}
	}

	@Override
	public T3 setupModelUpdateOrDelete(T3 o) {
		T3 r = super.setupModelUpdateOrDelete(o);
		setModelByGui(r);
		return r;
	}

	public TableView<GameClientFileTableItem> getClientFilesTable() {
		return clientFilesTable;
	}

	public void setClientFilesTable(
			TableView<GameClientFileTableItem> clientFilesTable) {
		this.clientFilesTable = clientFilesTable;
	}

}
