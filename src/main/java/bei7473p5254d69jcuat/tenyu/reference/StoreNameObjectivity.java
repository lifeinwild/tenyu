package bei7473p5254d69jcuat.tenyu.reference;

import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.item.*;
import bei7473p5254d69jcuat.tenyu.db.store.game.statebyuser.*;
import bei7473p5254d69jcuat.tenyu.db.store.sociality.*;
import jetbrains.exodus.env.*;

/**
 * 各ストアの名前。
 * これを使うとString storeNameのようなものを無くせる。
 * 特に永続化や通信において少しサイズを削減できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public enum StoreNameObjectivity implements StoreNameEnum {
	USER(txn -> new UserStore(txn), UserStore.modelName),
	WEB(txn -> new WebStore(txn), WebStore.modelName),
	ROLE(txn -> new RoleStore(txn), RoleStore.modelName),
	STATIC_GAME(txn -> new StaticGameStore(txn), StaticGameStore.modelName),
	STATIC_GAME_EQUIPMENT_CLASS(
			txn -> new StaticGameEquipmentClassStore(txn),
			StaticGameEquipmentClassStore.modelName),
	STATIC_GAME_MATERIAL_CLASS(
			txn -> new StaticGameMaterialClassStore(txn),
			StaticGameMaterialClassStore.modelName),
	RATING_GAME(txn -> new RatingGameStore(txn), RatingGameStore.modelName),
	RATING_GAME_EQUIPMENT_CLASS(
			txn -> new RatingGameEquipmentClassStore(txn),
			RatingGameEquipmentClassStore.modelName),
	RATING_GAME_STATE_BY_USER(
			txn -> new RatingGameStateByUserStore(txn),
			RatingGameStateByUserStore.modelName),
	STATIC_GAME_STATE_BY_USER(
			txn -> new StaticGameStateByUserStore(txn),
			StaticGameStateByUserStore.modelName),
	GAME_EQUIPMENT_CLASS(
			txn -> new RatingGameEquipmentClassStore(txn),
			RatingGameEquipmentClassStore.modelName),
	GAME_MATERIAL_CLASS(
			txn -> new StaticGameMaterialClassStore(txn),
			StaticGameMaterialClassStore.modelName),
	FLOW_NETWORK_ABSTRACT_NOMINAL(
			txn -> new FlowNetworkAbstractNominalStore(txn),
			FlowNetworkAbstractNominalStore.modelName),
	USER_MESSAGE_LIST_HASH(
			txn -> new UserMessageListHashStore(txn),
			UserMessageListHashStore.modelName),
	FREE_KVPAIR(txn -> new FreeKVPairStore(txn), FreeKVPairStore.modelName),
	SOCIALITY(txn -> new SocialityStore(txn), SocialityStore.modelName),
	DISTRIBUTED_TRADABLE(
			txn -> new DistributedTradableStore(txn),
			DistributedTradableStore.modelName),
	AGENDA(txn -> new AgendaStore(txn), AgendaStore.modelName),
	DISTRIBUTED_VOTE(
			txn -> new DistributedVoteStore(txn),
			DistributedVoteStore.modelName),
	SOCIALITY_INCOME_SHARING(
			txn -> new SocialityIncomeSharingStore(txn),
			SocialityIncomeSharingStore.modelName),
	RATING_GAME_MATCH(
			txn -> new RatingGameMatchStore(txn),
			RatingGameMatchStore.modelName),
	EDGE_LOG(txn -> new EdgeLogStore(txn), EdgeLogStore.modelName),
	URL_PROVEMENT_REGEX_STORE(
			txn -> new URLProvementRegexStore(txn),
			URLProvementRegexStore.modelName),
	DISTRIBUTED_VOTE_RESULT(
			txn -> new DistributedVoteResultStore(txn),
			DistributedVoteResultStore.modelName),;

	private final Function<Transaction,
			IdObjectStore<? extends IdObjectDBI, ?>> getStore;

	private final String modelname;

	private StoreNameObjectivity(
			Function<Transaction,
					IdObjectStore<? extends IdObjectDBI, ?>> getStore,
			String modelname) {
		this.getStore = getStore;
		this.modelname = modelname;
	}

	public IdObjectStore<? extends IdObjectDBI, ?> getStore(Transaction txn) {
		return getStore.apply(txn);
	}

	@Override
	public String getModelName() {
		return modelname;
	}

	public static boolean contains(StoreName n) {
		for (StoreNameObjectivity e : StoreNameObjectivity.values()) {
			if (e == n)
				return true;
		}
		return false;
	}

}
