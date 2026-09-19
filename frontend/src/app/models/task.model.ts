export interface Produto {
  nome: string;
  quantidade: number;
  valor: number;
}

export interface Task {
  id: number;
  nome: string;
  titulo?: string;
  status: 'Pendente' | 'Em andamento' | 'Concluída';
  prioridade: 'Alta' | 'Média' | 'Baixa';
  valor: number;
  descricao?: string;
  produtos?: any[];
  dataVencimento?: string;
}

export interface Login {
  senha: string;
  usuario: string;
  email: string;
}