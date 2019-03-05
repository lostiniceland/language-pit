package infrastructure.persistence;

import domain.wife.BikeApproval;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class JpaWifeApprovalStatusConverter implements AttributeConverter<BikeApproval.ApprovalStatus, Character> {

	@SuppressWarnings("Duplicates")
	@Override
	public Character convertToDatabaseColumn(BikeApproval.ApprovalStatus attribute) {
		switch (attribute) {
			case Pending:
				return 'P';
			case Accepted:
				return 'A';
			case Rejected:
				return 'R';
			default:
				throw new IllegalArgumentException("Unknown " + attribute);
		}
	}

	@Override
	public BikeApproval.ApprovalStatus convertToEntityAttribute(Character dbData) {
		switch (dbData) {
			case 'P':
				return BikeApproval.ApprovalStatus.Pending;
			case 'A':
				return BikeApproval.ApprovalStatus.Accepted;
			case 'R':
				return BikeApproval.ApprovalStatus.Rejected;
			default:
				throw new IllegalArgumentException("Unknown " + dbData);
		}
	}
}
