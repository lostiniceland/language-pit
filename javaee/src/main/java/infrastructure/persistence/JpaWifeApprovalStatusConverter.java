package infrastructure.persistence;

import domain.wife.ApprovalStatus;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class JpaWifeApprovalStatusConverter implements AttributeConverter<ApprovalStatus, Character> {

	@SuppressWarnings("Duplicates")
	@Override
	public Character convertToDatabaseColumn(ApprovalStatus attribute) {
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
	public ApprovalStatus convertToEntityAttribute(Character dbData) {
		switch (dbData) {
			case 'P':
				return ApprovalStatus.Pending;
			case 'A':
				return ApprovalStatus.Accepted;
			case 'R':
				return ApprovalStatus.Rejected;
			default:
				throw new IllegalArgumentException("Unknown " + dbData);
		}
	}
}
