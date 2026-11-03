package dev.aptitudes.client.contract;

import dev.aptitudes.network.ContractViewPayload;

public final class ContractClientState {
	private static ContractViewPayload view;
	private ContractClientState() {}
	public static void set(ContractViewPayload payload){view=payload;}
	public static ContractViewPayload get(){return view;}
	public static void clear(){view=null;}
}
