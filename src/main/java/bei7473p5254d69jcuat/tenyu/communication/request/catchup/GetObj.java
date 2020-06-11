package bei7473p5254d69jcuat.tenyu.communication.request.catchup;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.netty.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import glb.*;
import glb.util.*;
import io.netty.channel.*;

/**
 * 多数のオブジェクトをまとめて取得するメッセージ。
 * 1件ずつ取得する仕様にしていないのは、IDList内部で用いられている数値リストを
 * 圧縮するライブラリが優秀だから。その性能を活用したかった。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class GetObj extends AbstractByStoreMessage {
	/**
	 * 取得するオブジェクト1件あたりの最大サイズ
	 * 大きめに見る必要がある
	 */
	public static final int objSizeMax = 4000;

	/**
	 * idListを解凍した時のidの最大件数
	 */
	public static final int max = (int) (ChunkedDataConcatMemory.chunkedDataTotalSizeMax
			/ objSizeMax);

	/**
	 * 取得するオブジェクトのid一覧
	 */
	private IDList idList;

	/**
	 * 取得するオブジェクトのhid一覧
	 */
	private IDList hidList;

	public void setIdList(IDList idList) {
		this.idList = idList;
	}

	public void setHidList(IDList hidList) {
		this.hidList = hidList;
	}

	public IDList getHidList() {
		return hidList;
	}

	@Override
	protected final boolean validateAbstractByStoreMessageConcrete(Message m) {
		return idList != null || hidList != null;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof GetObjResponse;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		Glb.debug("storeName=" + storeName + " ids=" + idList + " hids="
				+ hidList);

		//返信されるオブジェクト一覧
		List<ModelI> objs = new ArrayList<>();

		//ID
		if (idList != null) {
			long[] ids = idList.uncompress();
			Glb.getObje().read(txn -> {
				ModelStore<? extends ModelI, ?> s = storeName.getStore(txn);
				if (s == null)
					return;
				int count = 0;
				for (Long id : ids) {
					ModelI o = s.getRawObj(id);
					if (o == null)
						continue;
					objs.add(o);
					count++;
					if (count > max)
						break;
				}
			});
		}

		//HID
		if (hidList != null) {
			long[] hids = hidList.uncompress();
			Glb.getObje().read(txn -> {
				ModelStore<? extends ModelI, ?> s = storeName.getStore(txn);
				if (s == null)
					return;
				int count = 0;
				for (Long hid : hids) {
					ModelI o = s.getRawObjByHid(hid);
					if (o == null)
						continue;
					objs.add(o);
					count++;
					if (count > max)
						break;
				}
			});
		}

		if (objs.size() == 0)
			return false;

		GetObjResponse res = new GetObjResponse();
		res.setStoreName(storeName);
		res.setObjs(objs);
		if (res.reduceSize()) {
			Glb.getLogger().info("GetObjResponse is reduced");
		}
		Message resM = Message.build(res)
				.packaging(res
						.createPackage(validated.getEdgeByInnermostPackage()))
				.finish();

		return Glb.getP2p().response(resM, ctx);
	}

	public IDList getIdList() {
		return idList;
	}

	public static class GetObjResponse extends AbstractByStoreMessageResponse {
		/**
		 * idまたはhidで取得されたオブジェクト一覧
		 * 要素のIDが{@link ModelI#getNullId()}と一致する場合、削除されたことを意味する
		 */
		private List<ModelI> objs;

		public void setObjs(List<ModelI> objs) {
			this.objs = objs;
		}

		public List<ModelI> getObjs() {
			return objs;
		}

		@Override
		protected final boolean validateAbstractByStoreMessageResponseConcrete(
				Message m) {
			return objs != null && objs.size() > 0;
		}

		@Override
		public boolean isValid(Request req) {
			return req instanceof GetObj;
		}

		@Override
		public boolean received(ChannelHandlerContext ctx, Received validated) {
			Glb.debug("GetObjResponse " + objs);
			return true;
		}

		/**
		 * もしthisのサイズが通信可能なサイズを超えていたら要素を少し減らす。
		 * @return	サイズ縮小が発生したか
		 */
		public boolean reduceSize() {
			long size = Glb.getUtil().sizeOf(this);
			Glb.debug("size=" + size);
			//ObjectSizeCalculator.getObjectSize(this);
			long tolerance = objSizeMax * 2;
			long distance = size + tolerance
					- ChunkedDataConcatMemory.chunkedDataTotalSizeMax;
			if (distance < 0) {
				return false;
			}

			long reduceCount = distance / objSizeMax;
			if (reduceCount > objs.size())
				reduceCount = objs.size() - 1;
			for (int i = 0; i < reduceCount; i++) {
				objs.remove(i);
			}
			return true;
		}

	}

	@Override
	public String toString() {
		return "GetObj [idList=" + idList + ", hidList=" + hidList + "]";
	}

}
