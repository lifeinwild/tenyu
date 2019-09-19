package bei7473p5254d69jcuat.tenyu.release1.ui.common;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import javafx.beans.property.*;

public class NaturalityTableItem<T1 extends NaturalityDBI, T2 extends T1>
		extends ObjectivityObjectTableItem<T1, T2> {
	private StringProperty explanation = new SimpleStringProperty();
	private StringProperty name = new SimpleStringProperty();

	public NaturalityTableItem(T2 src) {
		super(src);
		updateNaturalityDBITableItem();
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
		updateNaturalityDBITableItem();
	}

	public void updateNaturalityDBITableItem() {
		if (src == null)
			return;
		setName(src.getName());
		setExplanation(src.getExplanation());
	}
}