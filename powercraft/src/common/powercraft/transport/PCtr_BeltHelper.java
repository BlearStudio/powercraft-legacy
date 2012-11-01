package powercraft.transport;

public class PCtr_BeltHelper {

	/** belts' height in units. 0.0625F = one pixel in vanilla textures. */
	public static final float HEIGHT = 0.0625F;

	/**
	 * Get unified rotation number form belt's meta.
	 * 
	 * @param meta belt meta
	 * @return the rotation 0-3
	 */
	public static int getRotation(int meta) {
		switch (meta) {
			case 0:
			case 6:
				return 0;
			case 1:
			case 7:
				return 1;
			case 8:
			case 14:
				return 2;
			case 9:
			case 15:
				return 3;
		}
		return 0;
	}

}
