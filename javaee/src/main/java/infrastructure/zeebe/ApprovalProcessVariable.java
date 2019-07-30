package infrastructure.zeebe;

class ApprovalProcessVariable {

	long bikeId;
	float value;
	int bikesOwned;

	public ApprovalProcessVariable() {
	}


	public ApprovalProcessVariable(long bikeId, float value, int bikesOwned) {
		this.bikeId = bikeId;
		this.value = value;
		this.bikesOwned = bikesOwned;
	}

	public long getBikeId() {
		return bikeId;
	}

	public void setBikeId(long bikeId) {
		this.bikeId = bikeId;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public int getBikesOwned() {
		return bikesOwned;
	}

	public void setBikesOwned(int bikesOwned) {
		this.bikesOwned = bikesOwned;
	}
}
