package domain.bikes;

import domain.DomainEvent;

public class BikeApprovedEvent extends DomainEvent {

	long bikeId;

	public BikeApprovedEvent(long bikeId) {
		this.bikeId = bikeId;
	}

	public long getBikeId() {
		return bikeId;
	}
}
