package bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.tenyupedia;

import static bei7473p5254d69jcuat.tenyu.db.DBUtil.*;

import java.io.*;
import java.util.*;

import javax.management.modelmbean.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.tenyupedia.*;
import glb.*;
import glb.util.*;
import jetbrains.exodus.*;
import jetbrains.exodus.env.*;

public class TagStore extends IndividualityObjectStore<TagI, Tag> {
	public static final String modelName = Tag.class.getSimpleName();

	public TagStore(Transaction txn) {
		super(txn);
	}

	@Override
	public boolean isSelfReference() {
		return true;
	}

	@Override
	protected boolean createIndividualityObjectConcrete(TagI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean dbValidateAtUpdateIndividualityObjectConcrete(
			TagI updated, TagI old, ValidationResult r) {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean deleteIndividualityObjectConcrete(TagI o)
			throws Exception {
		return true;
	}

	@Override
	protected boolean existIndividualityObjectConcrete(TagI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	protected List<StoreInfo> getStoresIndividualityObjectConcrete() {
		List<StoreInfo> r = new ArrayList<>();
		return r;
	}

	@Override
	protected boolean noExistIndividualityObjectConcrete(TagI o,
			ValidationResult vr) throws Exception {
		boolean b = true;
		return b;
	}

	@Override
	protected boolean updateIndividualityObjectConcrete(TagI updated, TagI old)
			throws Exception {
		return true;
	}

	@Override
	public boolean isSupport(Object o) {
		if (o instanceof bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.tenyupedia.TagI)
			return true;
		return false;
	}

	@Override
	protected Tag chainversionup(ByteIterable bi) {
		try {
			if (bi == null)
				return null;
			Object o = cnvO(bi);
			if (o instanceof Tag)
				return (Tag) o;
			throw new InvalidTargetObjectTypeException(
					"not Tag object in WebStore");
		} catch (IOException | InvalidTargetObjectTypeException e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

	@Override
	public String getName() {
		return modelName;
	}

}
