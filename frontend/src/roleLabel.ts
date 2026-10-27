export function roleLabel(role: string): string {
  const name = role.startsWith('ROLE_') ? role.slice('ROLE_'.length) : role
  if (name === 'USER') return 'Игрок'
  if (name === 'ADMIN') return 'Администратор'
  return role
}
