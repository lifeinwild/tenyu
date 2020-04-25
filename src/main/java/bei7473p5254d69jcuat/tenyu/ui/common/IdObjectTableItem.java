package bei7473p5254d69jcuat.tenyu.ui.common;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import javafx.beans.property.*;

public abstract class IdObjectTableItem<T1 extends IdObjectI, T2 extends T1>
		implements TableRow<T2> {
	private LongProperty id = new SimpleLongProperty();
	protected T2 src;

	public IdObjectTableItem(T2 src) {
		this.src = src;
		updateIdObjectITableItem();
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
			id = IdObjectI.getExceptionalId();
		this.id.set(id);
	}

	public void update() {
		updateIdObjectITableItem();
	}

	public void updateIdObjectITableItem() {
		if (src == null)
			return;
		setId(src.getId());
	}

}