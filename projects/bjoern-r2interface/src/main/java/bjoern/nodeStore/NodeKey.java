package bjoern.nodeStore;

public class NodeKey
{
	private String type = "";
	private Long address = 0l;
	private Integer counter = null;

	public NodeKey(long address, String type)
	{
		setAddress(address);
		setType(type);
	}

	private void setType(String type)
	{
		this.type = type;
	}

	private void setAddress(Long address)
	{
		this.address = address;
	}

	@Override
	public String toString()
	{
		String key = this.type + "_" + Long.toHexString(this.address);
		return (counter == null) ? key : key + "_" + counter.toString();
	}

	public Long getAddress()
	{
		return this.address;
	}

}
