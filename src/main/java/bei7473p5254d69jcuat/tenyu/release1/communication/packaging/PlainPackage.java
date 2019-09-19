package bei7473p5254d69jcuat.tenyu.release1.communication.packaging;

import java.io.*;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;

/**
 * シリアライザによる単純なシリアライズとデシリアライズをする梱包。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class PlainPackage extends Package {
	public static interface PlainPackageContent {
		default Package createPackage() {
			return new PlainPackage();
		}
	}

	public PlainPackage() {
	}

	@Override
	public Communicatable deserializeConcrete(Message m) {
		Object o = Glb.getUtil().fromKryoBytesForCommunication(contentBinary);
		if (o != null && o instanceof Communicatable) {
			return (Communicatable) o;
		}
		return null;
	}

	@Override
	protected boolean binarizeAndSetContentConcrete(Communicatable content,
			Message m) {
		try {
			contentBinary = Glb.getUtil().toKryoBytesForCommunication(content);
			return true;
		} catch (IOException e) {
			Glb.getLogger()
					.error("contentBinary.length" + contentBinary == null
							? "null"
							: contentBinary.length, e);
		}
		return false;
	}

	@Override
	protected boolean isValidType(Object content) {
		return content instanceof PlainPackageContent;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlainPackage))
			return false;
		return super.equals(obj);
	}

	@Override
	protected final boolean validatePackageConcrete(Message m) {
		return true;
	}
}
