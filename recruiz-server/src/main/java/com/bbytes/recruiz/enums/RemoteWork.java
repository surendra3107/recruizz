package com.bbytes.recruiz.enums;

public enum RemoteWork {

	Yes(true), No(false);

	boolean remoteValue;

	private RemoteWork(boolean remoteValue) {
		this.remoteValue = remoteValue;
	}

	/**
	 * @return the remoteValue
	 */
	public boolean isRemoteValue() {
		return remoteValue;
	}

	/**
	 * @param remoteValue
	 *            the remoteValue to set
	 */
	public void setRemoteValue(boolean remoteValue) {
		this.remoteValue = remoteValue;
	}

}
