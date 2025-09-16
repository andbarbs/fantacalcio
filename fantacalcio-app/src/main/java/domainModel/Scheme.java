package domainModel;

import com.fantacalcio.app.generator.api.GenerateScheme;
import com.fantacalcio.app.generator.api.GenerateSchemes;

import domainModel.scheme.Scheme343;
import domainModel.scheme.Scheme433;
import domainModel.scheme.Scheme532;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@GenerateSchemes({
	@GenerateScheme(defenders = 4, midfielders = 3, forwards = 3),
    @GenerateScheme(defenders = 3, midfielders = 4, forwards = 3),
    @GenerateScheme(defenders = 5, midfielders = 3, forwards = 2)
})
public abstract class Scheme {

	private final int numDefenders;
	private final int numMidfielders;
	private final int numForwards;

	protected Scheme(int numDefenders, int numMidfielders, int numForwards) {
		this.numDefenders = numDefenders;
		this.numMidfielders = numMidfielders;
		this.numForwards = numForwards;
	}

	public int getNumDefenders() {
		return numDefenders;
	}

	public int getNumMidfielders() {
		return numMidfielders;
	}

	public int getNumForwards() {
		return numForwards;
	}
	
	public static interface SchemeVisitor {

		public void visitScheme433(Scheme433 scheme433);

		public void visitScheme343(Scheme343 scheme343);

		public void visitScheme532(Scheme532 scheme532);
		
	}
	
	public abstract void accept(SchemeVisitor visitor);
	
	@Converter
	public static class SchemeConverter implements AttributeConverter<Scheme, String> {

	    @Override
	    public String convertToDatabaseColumn(Scheme scheme) {
	        if (scheme == null) {
	            return null;
	        }
	        return String.format("%d-%d-%d", 
	        		scheme.getNumDefenders(), scheme.getNumMidfielders(), scheme.getNumForwards());
	    }

	    @Override
	    public Scheme convertToEntityAttribute(String dbData) {
	        if (dbData == null) {
	            return null;
	        }
	        return switch (dbData) {
	            case "4-3-3" -> Scheme433.INSTANCE;
	            case "3-4-3" -> Scheme343.INSTANCE;
	            case "5-3-2" -> Scheme532.INSTANCE;
	            default -> throw new IllegalArgumentException("Unknown scheme code: " + dbData);
	        };
	    }
	}
}
