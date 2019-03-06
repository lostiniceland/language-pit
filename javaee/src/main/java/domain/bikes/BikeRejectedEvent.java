package domain.bikes;

import domain.DomainEvent;

public class BikeRejectedEvent extends DomainEvent {

	long bikeId;

	public BikeRejectedEvent(long bikeId) {
		this.bikeId = bikeId;
	}

	public long getBikeId() {
		return bikeId;
	}
}
