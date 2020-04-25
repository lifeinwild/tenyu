package bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;

/**
 * GUI表示のためのP2PEdge + Userの情報
 * @author exceptiontenyu@gmail.com
 *
 */
public class P2PEdgeTableItem extends AbstractP2PEdgeAndUserTableItem<P2PEdge> {
	public P2PEdgeTableItem(P2PEdge src) {
		super(src);
		updateUser();
	}

	protected void updateUser() {
		Long userId = Glb.getObje().getUser(
				us -> us.getIdByAny(edge.getNode().getPubKey().getByteArray()));
		if (userId != null) {
			User u = Glb.getObje().getUser(us -> us.get(userId));
			setName(u.getName());
			setExplanation(u.getExplanation());
			setUserId(userId);
		} else {
			setUserId(IdObjectI.getNullId());
		}
	}

	@Override
	public P2PEdge getSrc() {
		return edge;
	}
}
