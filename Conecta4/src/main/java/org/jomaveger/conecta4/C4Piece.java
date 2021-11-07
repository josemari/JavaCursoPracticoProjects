package org.jomaveger.conecta4;

import org.jomaveger.game.Piece;

public enum C4Piece implements Piece {

	B, R, E;

	@Override
	public C4Piece opposite() {
		switch (this) {
		case B:
			return C4Piece.R;
		case R:
			return C4Piece.B;
		default:
			return C4Piece.E;
		}
	}

	@Override
	public String toString() {
		switch (this) {
		case B:
			return "B";
		case R:
			return "R";
		default:
			return " ";
		}
	}
}
