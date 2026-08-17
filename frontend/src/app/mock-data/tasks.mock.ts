import { Task } from "../models/task.model";

export const TASKS_MOCK: Task[] = [

    {
        id: 1,
        nome: 'Instalacao Portao Basculante',
        status: 'Em andamento',
        prioridade: 'Alta',
        valor: 3200,
        descricao: 'Instalacao de portao basculante  2.5m com motor PPA',
        produtos: [
            {nome: 'Motor PPA 1/4', quantidade: 1, valor:950},
            {nome: 'Portao basculante 2,5m', quantidade: 1, valor:1200},
        ]

    },

    {
      id: 2,
      nome: 'Configurar Camera Da Xiaomi',
      status: 'Aguardando',
      prioridade: 'Média',
      valor: 450,
      descricao: 'Configurar Rede de cameras novas da Xiaomi',
      produtos: [
        {nome: 'Camera Xiaomi', quantidade: 4, valor: 600}
      ]

    }


];