package bei7473p5254d69jcuat.tenyu.release1.ui.creator.game;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;

public abstract class AbstractGameTableItem<
		T1 extends AbstractGameDBI,
		T2 extends T1> extends NaturalityTableItem<T1, T2> {

	public AbstractGameTableItem(T2 src) {
		super(src);
	}

}