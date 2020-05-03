package bei7473p5254d69jcuat.tenyu.ui.common;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import javafx.beans.property.*;

public abstract class ModelTableItem<T1 extends ModelI, T2 extends T1>
		implements TableRow<T2> {
	private LongProperty id = new SimpleLongProperty();
	protected T2 src;

	public ModelTableItem(T2 src) {
		this.src = src;
		updateModelITableItem();
	}

	public Long getId() {
		return id.get();
	}

	@Override
	public T2 getSrc() {
		return src;
	}

	public void setId(Long id) {
		if (id == null)
			id = ModelI.getExceptionalId();
		this.id.set(id);
	}

	public void update() {
		updateModelITableItem();
	}

	public void updateModelITableItem() {
		if (src == null)
			return;
		setId(src.getId());
	}

}