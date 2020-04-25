package bei7473p5254d69jcuat.tenyu.ui.common;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import glb.*;
import javafx.beans.property.*;

public class IndividualityObjectTableItem<T1 extends IndividualityObjectI, T2 extends T1>
		extends AdministratedObjectTableItem<T1, T2> {
	private StringProperty explanation = new SimpleStringProperty();
	private StringProperty name = new SimpleStringProperty();

	public IndividualityObjectTableItem(T2 src) {
		super(src);
		updateIndividualityObjectITableItem();
	}

	public String getExplanation() {
		return explanation.get();
	}

	public String getName() {
		return name.get();
	}

	@Override
	public T2 getSrc() {
		return src;
	}

	public void setExplanation(String explanation) {
		if (explanation == null)
			explanation = "";
		try {
			//行数制限
			int limit = 3;
			String[] lines = explanation.split(System.lineSeparator(),
					limit + 1);
			StringBuilder sb = new StringBuilder();
			String ls = null;
			for (int i = 0; i < limit && i < lines.length; i++) {
				sb.append(ls + lines[i]);
				if (ls == null)
					ls = System.lineSeparator();
			}
			this.explanation.set(sb.toString());
		} catch (Exception e) {
			Glb.debug(e);
		}
	}

	public void setName(String name) {
		if (name == null)
			name = "";
		this.name.set(name);
	}

	@Override
	public void update() {
		super.update();
		updateIndividualityObjectITableItem();
	}

	public void updateIndividualityObjectITableItem() {
		if (src == null)
			return;
		setName(src.getName());
		setExplanation(src.getExplanation());
	}
}