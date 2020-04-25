package bei7473p5254d69jcuat.tenyutalk;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.other.*;
import glb.*;

/**
 * {@link MultiplayerObjectI}のオンメモリ管理
 * @author exceptiontenyu@gmail.com
 *
 */
public class OnmemoryManagementMultiplayer {
	/**
	 * このオンメモリ管理内のチャットメッセージのID
	 */
	private AtomicLong chatIdGenerator = new AtomicLong();

	private ConcurrentMap<Long,
			MultiplayerObjectI> multiplayers = new ConcurrentHashMap<>();

	/**
	 * {@link ChatMessageI}のオンメモリ管理
	 */
	private ConcurrentMap<Long,
			ChatMessage> chatMessages = new ConcurrentHashMap<>();

	/**
	 * @param id	このIDのオブジェクトを取得
	 * @return	オンメモリ管理されている多人数参加コンテンツ
	 */
	public MultiplayerObjectI getMultiplayerObject(Long id) {
		return multiplayers.get(id);
	}

	/**
	 * 多人数参加コンテンツを開催する
	 * @param o	開催される多人数参加コンテンツ
	 * @return	追加できたか
	 */
	public boolean hold(MultiplayerObjectI o) {
		multiplayers.put(o.getId(), o);
		return true;
	}

	/**
	 * 多人数参加コンテンツを終了または中断し、セーブする
	 * @param id
	 * @return
	 */
	public boolean closeAndSave(Long id) {
		MultiplayerObjectI o = getMultiplayerObject(id);
		if (o == null) {
			Glb.getLogger().warn("obj is not found. id=" + id,
					new IllegalStateException());
			return false;
		}
		multiplayers.remove(id);
		Lock l = o.getLock();
		l.lock();
		try {
			if (!o.save())
				throw new IOException("Failed to save");
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
		} finally {
			l.unlock();
		}
		return false;
	}

}
