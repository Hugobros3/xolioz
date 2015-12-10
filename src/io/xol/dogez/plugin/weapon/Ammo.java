package io.xol.dogez.plugin.weapon;

import io.xol.dogez.plugin.DogeZPlugin;
import io.xol.dogez.plugin.misc.NMSTools;

//Copyright 2014 XolioWare Interactive

public class Ammo {

	public static void init(DogeZPlugin dogeZPlugin) 
	{
		//Armes de base
		Ammo.addAmmo(new Ammo(370,0,"shotgun",12));
		Ammo.addAmmo(new Ammo(376,0,"svd",10));
		Ammo.addAmmo(new Ammo(372,0,"ak74",30));
		Ammo.addAmmo(new Ammo(375,0,"m4",30));
		Ammo.addAmmo(new Ammo(371,0,"m1911",7));
		Ammo.addAmmo(new Ammo(353,0,"makarov",7));
		Ammo.addAmmo(new Ammo(406,0,"dmr",20));
		Ammo.addAmmo(new Ammo(289,0,"akm",30));
		//ISIS
		Ammo.addAmmo(new Ammo(336,0,"aa12",15));
		Ammo.addAmmo(new Ammo(337,0,"uzi",20));
		Ammo.addAmmo(new Ammo(348,0,"pkm",50));
		//Special
		Ammo.addAmmo(new Ammo(318,0,"m9sd",15));
		Ammo.addAmmo(new Ammo(388,0,"m249",60));
		System.out.println("[DogeZ-Plugin] "+ammoC+" ammo types initialized.");
	}
	
	public static Ammo[]ammoId = new Ammo[512*16];
	public static int ammoC = 0;
	
	public static Ammo addAmmo(Ammo ammo)
	{
		ammoId[ammo.id*16+ammo.meta] = ammo;
		ammoC++;
		Weapon.getWeapon(ammo.weapon).addSuitableAmmo(ammo);
		NMSTools.setMaxStackSize(ammo.id, ammo.magSize);
		return ammo;
	}

	public static Ammo getAmmo(int id, int meta) {
		id = id%512;
		return ammoId[id*16+meta%16];
	}
	
	public static boolean isAmmo(int id, int meta)
	{
		id = id%512;
		if(id <= 0)
			return false;
		return !(ammoId[id*16+meta%16] == null);
	}
	
	public int id;
	public int meta;
	public String weapon;
	public int magSize;
	
	public Ammo(int id, int meta, String weapon, int magSize)
	{
		this.id = id;
		this.meta = meta;
		this.weapon = weapon;
		this.magSize = magSize;
	}
}
