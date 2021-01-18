package fr.iban.warps.utils;

import java.util.Comparator;

import fr.iban.warps.Warp;

public class NoteComparator implements Comparator<Warp>{

	@Override
	public int compare(Warp a, Warp b) {
		return (int) (Math.round(a.getNote()*100) - Math.round(b.getNote()*100));
	}

}
