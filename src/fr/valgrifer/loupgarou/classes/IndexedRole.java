package fr.valgrifer.loupgarou.classes;

import fr.valgrifer.loupgarou.roles.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndexedRole {
	@Getter private final Role role;
	@Getter private int number = 1;
	public void increase() {
		number++;
	}
}
