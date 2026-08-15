

export interface Produto {
    nome: string;
    quantidade: number;
    valor: number;
}

export interface Task {
    id: number;
    nome: string;
    status: 'Em andamento' | 'Aguardando' | 'Concluída';
    prioridade: 'Alta' | 'Média' | 'Baixa';
    valor: number;
    descricao: string;
    produtos: Produto[];
}